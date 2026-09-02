package com.dreambotreborn.api.methods.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.wrappers.widgets.Widget;

/** Widget lookup and filtering API. */
public final class Widgets
{
    private static volatile List<Widget> snapshot = Collections.emptyList();
    private static volatile int selectedWidgetId = -1;
    private static volatile int selectedWidgetIndex = -1;

    private Widgets()
    {
    }

    public static Query<Widget> all()
    {
        return new Query<>(snapshot);
    }

    public static Query<Widget> find(Predicate<? super Widget> predicate)
    {
        return all().filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static Query<Widget> visible()
    {
        return find(Widget::isVisible);
    }

    public static List<com.dreambotreborn.api.methods.widget.Widget> getAllWidgets()
    {
        java.util.LinkedHashSet<Integer> groups = new java.util.LinkedHashSet<>();
        for (Widget widget : snapshot) groups.add(widget.groupId);
        List<com.dreambotreborn.api.methods.widget.Widget> result = new ArrayList<>();
        for (Integer group : groups) result.add(new com.dreambotreborn.api.methods.widget.Widget(group));
        return Collections.unmodifiableList(result);
    }

    public static com.dreambotreborn.api.methods.widget.Widget getWidget(int groupId)
    {
        return snapshot.stream().anyMatch(widget -> widget.groupId == groupId)
            ? new com.dreambotreborn.api.methods.widget.Widget(groupId) : null;
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getChildWidget(
        int groupId, int childId)
    {
        int packed = (groupId << 16) | (childId & 0xffff);
        Widget widget = find(value -> value.id == packed).first();
        return child(widget);
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild get(int... path)
    {
        if (path == null || path.length == 0)
        {
            return null;
        }
        net.runelite.api.widgets.Widget live;
        if (path.length == 1)
        {
            live = DreamBotRebornApi.requireClient().getWidget(path[0]);
        }
        else
        {
            live = DreamBotRebornApi.requireClient().getWidget(path[0], path[1]);
            for (int i = 2; live != null && i < path.length; i++)
            {
                live = live.getChild(path[i]);
            }
        }
        return live == null ? null : new com.dreambotreborn.api.wrappers.widgets.WidgetChild(live);
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getWidgetChild(int... path)
    {
        return get(path);
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild get(
        Predicate<? super com.dreambotreborn.api.wrappers.widgets.WidgetChild> predicate)
    {
        return getMatchingWidget(predicate);
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild get(
        int groupId,
        Predicate<? super com.dreambotreborn.api.wrappers.widgets.WidgetChild> predicate)
    {
        if (predicate == null) return null;
        for (Widget widget : snapshot)
        {
            com.dreambotreborn.api.wrappers.widgets.WidgetChild child = child(widget);
            if (widget.groupId == groupId && predicate.test(child)) return child;
        }
        return null;
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getMatchingWidget(
        Predicate<? super com.dreambotreborn.api.wrappers.widgets.WidgetChild> predicate)
    {
        if (predicate == null) return null;
        for (Widget widget : snapshot)
        {
            com.dreambotreborn.api.wrappers.widgets.WidgetChild child = child(widget);
            if (predicate.test(child)) return child;
        }
        return null;
    }

    public static List<com.dreambotreborn.api.wrappers.widgets.WidgetChild> getAll(
        Predicate<? super com.dreambotreborn.api.wrappers.widgets.WidgetChild> predicate)
    {
        List<com.dreambotreborn.api.wrappers.widgets.WidgetChild> result = new ArrayList<>();
        if (predicate != null) for (Widget widget : snapshot)
        {
            com.dreambotreborn.api.wrappers.widgets.WidgetChild child = child(widget);
            if (predicate.test(child)) result.add(child);
        }
        return Collections.unmodifiableList(result);
    }

    public static boolean isVisible(int... path)
    {
        Widget widget = get(path);
        return widget != null && widget.visible;
    }

    public static List<com.dreambotreborn.api.wrappers.widgets.WidgetChild> getAllContainingText(
        String text)
    {
        String needle = text == null ? "" : text.toLowerCase();
        return getAll(widget -> widget.text.toLowerCase().contains(needle));
    }

    public static List<com.dreambotreborn.api.wrappers.widgets.WidgetChild>
        getWidgetChildrenContainingText(String text)
    {
        return getAllContainingText(text);
    }

    public static List<com.dreambotreborn.api.wrappers.widgets.WidgetChild> getWidgets(
        Predicate<? super com.dreambotreborn.api.wrappers.widgets.WidgetChild> predicate)
    {
        return getAll(predicate);
    }

    public static boolean isWidgetSelected()
    {
        return DreamBotRebornApi.requireClient().isWidgetSelected();
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getSelectedWidget()
    {
        net.runelite.api.widgets.Widget selected = DreamBotRebornApi.requireClient().getSelectedWidget();
        return selected == null ? null
            : new com.dreambotreborn.api.wrappers.widgets.WidgetChild(selected);
    }

    public static boolean isOpen() { return !snapshot.isEmpty(); }
    public static boolean closeAll()
    {
        if (!isOpen()) return true;
        com.dreambotreborn.api.methods.input.Keyboard.pressEsc();
        return true;
    }
    public static int[] getWidgetXArray()
    {
        int[] result = new int[snapshot.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = snapshot.get(i).bounds == null ? -1 : snapshot.get(i).bounds.x;
        return result;
    }
    public static int[] getWidgetYArray()
    {
        int[] result = new int[snapshot.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = snapshot.get(i).bounds == null ? -1 : snapshot.get(i).bounds.y;
        return result;
    }
    public static int getSelectedWidgetId()
    {
        Widget selected = getSelectedWidget();
        return selected == null ? selectedWidgetId : selected.id;
    }
    public static void setSelectedWidgetId(int value) { selectedWidgetId = value; }
    public static int getSelectedWidgetIndex()
    {
        Widget selected = getSelectedWidget();
        return selected == null ? selectedWidgetIndex : selected.index;
    }
    public static void setSelectedWidgetIndex(int value) { selectedWidgetIndex = value; }
    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getSelected()
    {
        net.runelite.api.widgets.Widget selected = DreamBotRebornApi.requireClient().getSelectedWidget();
        return selected == null ? null : new com.dreambotreborn.api.wrappers.widgets.WidgetChild(selected);
    }

    public static void refresh()
    {
        net.runelite.api.widgets.Widget[] roots;
        try
        {
            // The injected client's root table is unavailable for a short window
            // during bootstrap, even though individual login widgets are usable.
            roots = DreamBotRebornApi.requireClient().getWidgetRoots();
        }
        catch (NullPointerException unavailableDuringBootstrap)
        {
            clear();
            return;
        }
        if (roots == null)
        {
            clear();
            return;
        }
        List<Widget> result = new ArrayList<>();
        List<net.runelite.api.widgets.Widget> stack = new ArrayList<>();
        Set<net.runelite.api.widgets.Widget> seen =
            Collections.newSetFromMap(new IdentityHashMap<>());
        add(stack, roots);
        for (int cursor = 0; cursor < stack.size(); cursor++)
        {
            net.runelite.api.widgets.Widget widget = stack.get(cursor);
            if (!seen.add(widget))
            {
                continue;
            }
            result.add(new Widget(widget));
            add(stack, widget.getChildren());
            add(stack, widget.getDynamicChildren());
            add(stack, widget.getStaticChildren());
            add(stack, widget.getNestedChildren());
        }
        snapshot = Collections.unmodifiableList(result);
    }

    public static void clear()
    {
        snapshot = Collections.emptyList();
    }

    private static void add(
        List<net.runelite.api.widgets.Widget> target,
        net.runelite.api.widgets.Widget[] widgets)
    {
        if (widgets != null)
        {
            for (net.runelite.api.widgets.Widget widget : widgets)
            {
                if (widget != null)
                {
                    target.add(widget);
                }
            }
        }
    }

    private static com.dreambotreborn.api.wrappers.widgets.WidgetChild child(Widget widget)
    {
        return widget == null ? null
            : new com.dreambotreborn.api.wrappers.widgets.WidgetChild(widget.unwrap());
    }
}
