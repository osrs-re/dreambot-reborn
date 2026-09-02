package com.dreambotreborn.devtools;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;

/** Captures the live widget tree and renders developer selection overlays. */
public final class WidgetInspector
{
    private static final long REFRESH_INTERVAL_NANOS = 500_000_000L;
    private static final int MAX_WIDGETS = 12_000;
    private static final int MAX_DEPTH = 64;
    private static final int MAX_WIDGET_GROUPS = 1_024;
    private static final int GROUP_PROBE_CHILDREN = 8;
    private static final int MAX_GROUP_CHILDREN = 2_048;
    private static final long OBSERVED_WIDGET_LIFETIME_NANOS = 2_000_000_000L;
    private static final Color SELECTED_FILL = new Color(255, 174, 45, 68);
    private static final Color SELECTED_BORDER = new Color(255, 184, 54);
    private static final Color HOVER_FILL = new Color(30, 210, 230, 46);
    private static final Color HOVER_BORDER = new Color(58, 224, 238);

    private static volatile Client client;
    private static volatile WidgetTreeSnapshot snapshot = WidgetTreeSnapshot.EMPTY;
    private static volatile String selectedKey;
    private static volatile WidgetSnapshot selectedWidget;
    private static volatile WidgetSnapshot hoveredWidget;
    private static volatile boolean highlightEnabled = true;
    private static volatile boolean pickerEnabled;
    private static volatile boolean refreshRequested = true;
    private static final Map<Widget, Long> OBSERVED_WIDGETS = new IdentityHashMap<>();
    private static long nextRefreshNanos;
    private static long version;
    private static boolean refreshFailed;

    private WidgetInspector()
    {
    }

    public static synchronized void initialize(Client value)
    {
        client = Objects.requireNonNull(value, "client");
        snapshot = WidgetTreeSnapshot.EMPTY;
        selectedKey = null;
        selectedWidget = null;
        hoveredWidget = null;
        pickerEnabled = false;
        highlightEnabled = true;
        refreshRequested = true;
        OBSERVED_WIDGETS.clear();
        nextRefreshNanos = 0L;
        version = 0L;
        refreshFailed = false;
    }

    /** Called on the game thread. */
    public static void tick()
    {
        Client currentClient = client;
        if (currentClient == null)
        {
            return;
        }

        long now = System.nanoTime();
        if (!refreshRequested && now < nextRefreshNanos)
        {
            return;
        }
        refreshRequested = false;
        nextRefreshNanos = now + REFRESH_INTERVAL_NANOS;

        try
        {
            Capture capture = new Capture();
            RootResult rootResult = roots(currentClient);
            List<Widget> widgetRoots = rootResult.widgets;
            List<WidgetSnapshot> roots = new ArrayList<>();
            for (int rootIndex = 0; rootIndex < widgetRoots.size() && capture.count < MAX_WIDGETS; rootIndex++)
            {
                Widget root = widgetRoots.get(rootIndex);
                if (root != null)
                {
                    WidgetSnapshot rootSnapshot = capture.capture(
                        root, "root", "root-" + root.getId() + '-' + root.getIndex(), 0,
                        Collections.newSetFromMap(new IdentityHashMap<>()));
                    if (rootSnapshot != null)
                    {
                        roots.add(rootSnapshot);
                    }
                }
            }

            String source = rootResult.source;
            if (roots.isEmpty())
            {
                WidgetSnapshot loginRoot = LoginScreenInspector.capture(currentClient);
                if (loginRoot != null)
                {
                    roots.add(loginRoot);
                    capture.count = count(loginRoot);
                    source = "login UI adapter";
                }
            }

            WidgetTreeSnapshot updated = new WidgetTreeSnapshot(
                ++version, roots, capture.count, source);
            snapshot = updated;
            selectedWidget = findByKey(updated.roots, selectedKey);
            if (pickerEnabled && hoveredWidget != null)
            {
                hoveredWidget = findByKey(updated.roots, hoveredWidget.key);
            }
            refreshFailed = false;
        }
        catch (RuntimeException ex)
        {
            if (!refreshFailed)
            {
                System.err.println("Unable to refresh the widget inspector");
                ex.printStackTrace(System.err);
                refreshFailed = true;
            }
        }
    }

    public static WidgetTreeSnapshot getSnapshot()
    {
        return snapshot;
    }

    public static void requestRefresh()
    {
        refreshRequested = true;
    }

    /** Records a layer the game has actually rendered during this frame. */
    public static void observeLayer(Widget layer)
    {
        if (layer != null)
        {
            OBSERVED_WIDGETS.put(layer, System.nanoTime());
        }
    }

    /** Records an interface passed through RuneLite's draw callback. */
    public static void observeInterface(int interfaceId)
    {
        Client currentClient = client;
        if (currentClient == null || interfaceId < 0)
        {
            return;
        }
        Widget widget = currentClient.getWidget(interfaceId);
        if (widget == null && interfaceId < MAX_WIDGET_GROUPS)
        {
            widget = currentClient.getWidget(interfaceId, 0);
        }
        observeLayer(widget);
    }

    public static void onEvent(Object event)
    {
        if (event instanceof WidgetLoaded || event instanceof WidgetClosed)
        {
            requestRefresh();
        }
    }

    public static void select(String key)
    {
        selectedKey = key;
        selectedWidget = findByKey(snapshot.roots, key);
    }

    public static String getSelectedKey()
    {
        return selectedKey;
    }

    public static WidgetSnapshot getSelectedWidget()
    {
        return selectedWidget;
    }

    public static void setHighlightEnabled(boolean enabled)
    {
        highlightEnabled = enabled;
    }

    public static boolean isHighlightEnabled()
    {
        return highlightEnabled;
    }

    public static void setPickerEnabled(boolean enabled)
    {
        pickerEnabled = enabled;
        if (!enabled)
        {
            hoveredWidget = null;
        }
    }

    public static boolean isPickerEnabled()
    {
        return pickerEnabled;
    }

    /** Intercepts physical canvas input while the widget picker is active. */
    public static MouseEvent filterMouseEvent(MouseEvent event, boolean updateHover, boolean select)
    {
        if (event == null || event.isConsumed() || !pickerEnabled)
        {
            return event;
        }

        if (updateHover || select)
        {
            WidgetSnapshot picked = pick(event.getX(), event.getY());
            hoveredWidget = picked;
            if (select && picked != null)
            {
                WidgetInspector.select(picked.key);
            }
        }
        event.consume();
        return event;
    }

    public static MouseWheelEvent filterMouseWheelEvent(MouseWheelEvent event)
    {
        if (event != null && !event.isConsumed() && pickerEnabled)
        {
            event.consume();
        }
        return event;
    }

    public static void clearPickerHover()
    {
        hoveredWidget = null;
    }

    /** Drawn into the game's main buffer before it is copied to the canvas. */
    public static void draw(Graphics graphics)
    {
        if (graphics == null)
        {
            return;
        }

        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            WidgetSnapshot selected = selectedWidget;
            WidgetSnapshot hovered = hoveredWidget;
            if (highlightEnabled && selected != null)
            {
                drawHighlight(g, selected, SELECTED_FILL, SELECTED_BORDER, false, true);
            }
            if (pickerEnabled && hovered != null && (selected == null || !hovered.key.equals(selected.key)))
            {
                drawHighlight(g, hovered, HOVER_FILL, HOVER_BORDER, true, false);
            }
        }
        finally
        {
            g.dispose();
        }
    }

    private static void drawHighlight(
        Graphics2D g,
        WidgetSnapshot widget,
        Color fill,
        Color border,
        boolean dashed,
        boolean label)
    {
        Rectangle bounds = widget.bounds;
        if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
        {
            return;
        }

        g.setComposite(AlphaComposite.SrcOver);
        g.setColor(fill);
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        g.setColor(border);
        g.setStroke(dashed
            ? new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {5f, 4f}, 0f)
            : new BasicStroke(2.5f));
        g.drawRect(bounds.x, bounds.y, Math.max(0, bounds.width - 1), Math.max(0, bounds.height - 1));

        if (!label)
        {
            return;
        }

        String text = widget.isLoginElement()
            ? widget.identifier()
            : "Widget " + widget.identifier();
        FontMetrics metrics = g.getFontMetrics();
        int labelWidth = metrics.stringWidth(text) + 10;
        int labelHeight = metrics.getHeight() + 4;
        int labelX = Math.max(0, bounds.x);
        int labelY = bounds.y - labelHeight >= 0 ? bounds.y - labelHeight : bounds.y;
        g.setColor(new Color(22, 23, 26, 225));
        g.fillRoundRect(labelX, labelY, labelWidth, labelHeight, 5, 5);
        g.setColor(border);
        g.drawString(text, labelX + 5, labelY + metrics.getAscent() + 2);
    }

    private static WidgetSnapshot pick(int x, int y)
    {
        Pick best = new Pick();
        for (WidgetSnapshot root : snapshot.roots)
        {
            pick(root, x, y, 0, best);
        }
        return best.widget;
    }

    private static void pick(WidgetSnapshot widget, int x, int y, int depth, Pick best)
    {
        Rectangle bounds = widget.bounds;
        if (!widget.hidden && bounds != null && bounds.width > 0 && bounds.height > 0 && bounds.contains(x, y))
        {
            long area = (long) bounds.width * bounds.height;
            if (depth > best.depth || depth == best.depth && area <= best.area)
            {
                best.widget = widget;
                best.depth = depth;
                best.area = area;
            }
        }
        for (WidgetSnapshot child : widget.children)
        {
            pick(child, x, y, depth + 1, best);
        }
    }

    private static WidgetSnapshot findByKey(List<WidgetSnapshot> widgets, String key)
    {
        if (key == null)
        {
            return null;
        }
        for (WidgetSnapshot widget : widgets)
        {
            if (key.equals(widget.key))
            {
                return widget;
            }
            WidgetSnapshot child = findByKey(widget.children, key);
            if (child != null)
            {
                return child;
            }
        }
        return null;
    }

    private static int count(WidgetSnapshot widget)
    {
        int result = 1;
        for (WidgetSnapshot child : widget.children)
        {
            result += count(child);
        }
        return result;
    }

    /**
     * The injected client's normal root lookup depends on its top-level
     * interface node, which is temporarily null during this minimal startup.
     * Fall back to the loaded widget groups instead of publishing an empty tree.
     */
    private static RootResult roots(Client currentClient)
    {
        try
        {
            Widget[] directRoots = currentClient.getWidgetRoots();
            if (directRoots != null && directRoots.length > 0)
            {
                List<Widget> result = new ArrayList<>(directRoots.length);
                for (Widget widget : directRoots)
                {
                    if (widget != null)
                    {
                        result.add(widget);
                    }
                }
                if (!result.isEmpty())
                {
                    return new RootResult(result, "active roots");
                }
            }
        }
        catch (NullPointerException ignored)
        {
            // The interface manager is not fully attached yet; use the safe API below.
        }

        List<Widget> rendered = renderedRoots();
        if (!rendered.isEmpty())
        {
            return new RootResult(rendered, "rendered layers");
        }

        List<Widget> loaded = new ArrayList<>();
        Set<Widget> loadedIdentity = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int group = 0; group < MAX_WIDGET_GROUPS; group++)
        {
            boolean loadedGroup = false;
            for (int child = 0; child < GROUP_PROBE_CHILDREN; child++)
            {
                Widget widget = currentClient.getWidget(group, child);
                if (widget != null)
                {
                    loadedGroup = true;
                    if (loadedIdentity.add(widget))
                    {
                        loaded.add(widget);
                    }
                }
            }
            if (!loadedGroup)
            {
                continue;
            }
            for (int child = GROUP_PROBE_CHILDREN; child < MAX_GROUP_CHILDREN; child++)
            {
                Widget widget = currentClient.getWidget(group, child);
                if (widget != null && loadedIdentity.add(widget))
                {
                    loaded.add(widget);
                }
            }
        }

        Set<Widget> referencedChildren = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Widget widget : loaded)
        {
            addAll(referencedChildren, widget.getStaticChildren());
            addAll(referencedChildren, widget.getDynamicChildren());
            addAll(referencedChildren, widget.getNestedChildren());
        }

        List<Widget> result = new ArrayList<>();
        for (Widget widget : loaded)
        {
            if (!referencedChildren.contains(widget))
            {
                result.add(widget);
            }
        }
        return new RootResult(result, result.isEmpty()
            ? "widget system inactive"
            : "loaded groups");
    }

    private static List<Widget> renderedRoots()
    {
        long oldest = System.nanoTime() - OBSERVED_WIDGET_LIFETIME_NANOS;
        Iterator<Map.Entry<Widget, Long>> iterator = OBSERVED_WIDGETS.entrySet().iterator();
        while (iterator.hasNext())
        {
            if (iterator.next().getValue() < oldest)
            {
                iterator.remove();
            }
        }

        List<Widget> observed = new ArrayList<>(OBSERVED_WIDGETS.keySet());
        Set<Widget> referenced = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Widget widget : observed)
        {
            addAll(referenced, widget.getStaticChildren());
            addAll(referenced, widget.getDynamicChildren());
            addAll(referenced, widget.getNestedChildren());
        }
        List<Widget> roots = new ArrayList<>();
        for (Widget widget : observed)
        {
            if (!referenced.contains(widget))
            {
                roots.add(widget);
            }
        }
        roots.sort((left, right) ->
        {
            int idOrder = Integer.compare(left.getId(), right.getId());
            return idOrder != 0 ? idOrder : Integer.compare(left.getIndex(), right.getIndex());
        });
        return roots;
    }

    private static void addAll(Set<Widget> destination, Widget[] widgets)
    {
        if (widgets == null)
        {
            return;
        }
        for (Widget widget : widgets)
        {
            if (widget != null)
            {
                destination.add(widget);
            }
        }
    }

    private static String typeName(int type)
    {
        switch (type)
        {
            case WidgetType.LAYER:
                return "Layer";
            case WidgetType.RECTANGLE:
                return "Rectangle";
            case WidgetType.TEXT:
                return "Text";
            case WidgetType.GRAPHIC:
                return "Graphic";
            case WidgetType.MODEL:
                return "Model";
            case WidgetType.TEXT_INVENTORY:
                return "Inventory";
            case WidgetType.IF1_TOOLTIP:
                return "Tooltip";
            case WidgetType.LINE:
                return "Line";
            case WidgetType.INPUT_FIELD:
                return "Input";
            default:
                return "Type " + type;
        }
    }

    private static String safe(String value)
    {
        return value == null ? "" : value;
    }

    private static List<String> actions(String[] values)
    {
        if (values == null || values.length == 0)
        {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (String value : values)
        {
            if (value != null && !value.trim().isEmpty())
            {
                result.add(value);
            }
        }
        return result;
    }

    private static final class Capture
    {
        private int count;

        private WidgetSnapshot capture(
            Widget widget,
            String relation,
            String parentKey,
            int depth,
            Set<Widget> ancestors)
        {
            if (widget == null || count >= MAX_WIDGETS || depth > MAX_DEPTH || !ancestors.add(widget))
            {
                return null;
            }

            try
            {
                count++;
                int id = widget.getId();
                int index = widget.getIndex();
                String key = parentKey + '/' + relation + '-' + id + '-' + index;
                List<WidgetSnapshot> children = new ArrayList<>();
                Set<Widget> included = Collections.newSetFromMap(new IdentityHashMap<>());
                captureChildren(widget.getStaticChildren(), "static", key, depth, ancestors, included, children);
                captureChildren(widget.getDynamicChildren(), "dynamic", key, depth, ancestors, included, children);
                captureChildren(widget.getNestedChildren(), "nested", key, depth, ancestors, included, children);

                Rectangle bounds = widget.getBounds();
                return new WidgetSnapshot(
                    key,
                    relation,
                    id,
                    index,
                    widget.getType(),
                    typeName(widget.getType()),
                    widget.getContentType(),
                    safe(widget.getName()),
                    safe(widget.getText()),
                    bounds,
                    widget.isHidden(),
                    widget.isSelfHidden(),
                    widget.getItemId(),
                    widget.getItemQuantity(),
                    widget.getScrollX(),
                    widget.getScrollY(),
                    widget.getScrollWidth(),
                    widget.getScrollHeight(),
                    actions(widget.getActions()),
                    children);
            }
            finally
            {
                ancestors.remove(widget);
            }
        }

        private void captureChildren(
            Widget[] widgets,
            String relation,
            String parentKey,
            int depth,
            Set<Widget> ancestors,
            Set<Widget> included,
            List<WidgetSnapshot> destination)
        {
            if (widgets == null)
            {
                return;
            }
            for (Widget child : widgets)
            {
                if (child == null || !included.add(child) || count >= MAX_WIDGETS)
                {
                    continue;
                }
                WidgetSnapshot childSnapshot = capture(child, relation, parentKey, depth + 1, ancestors);
                if (childSnapshot != null)
                {
                    destination.add(childSnapshot);
                }
            }
        }
    }

    private static final class Pick
    {
        private WidgetSnapshot widget;
        private int depth = -1;
        private long area = Long.MAX_VALUE;
    }

    private static final class RootResult
    {
        private final List<Widget> widgets;
        private final String source;

        private RootResult(List<Widget> widgets, String source)
        {
            this.widgets = widgets;
            this.source = source;
        }
    }
}
