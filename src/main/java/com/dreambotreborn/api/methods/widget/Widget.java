package com.dreambotreborn.api.methods.widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

/** A top-level interface group and its children. */
public class Widget
{
    private final int id;

    public Widget(int id) { this.id = id; }
    public Widget(int id, Object[] ignored) { this(id); }
    public int getId() { return id; }
    public int getID() { return id; }
    public WidgetChild getChild(int child)
    {
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(id, child);
        return widget == null ? null : new WidgetChild(widget);
    }
    public List<WidgetChild> getChildren()
    {
        List<WidgetChild> result = new ArrayList<>();
        for (com.dreambotreborn.api.wrappers.widgets.Widget widget : Widgets.all())
            if (widget.groupId == id) result.add(new WidgetChild(widget.unwrap()));
        return Collections.unmodifiableList(result);
    }
    public Collection<WidgetChild> getChildrenCollection() { return getChildren(); }
    public void addWidgetChild(int child, WidgetChild widget) { }
    public void close()
    {
        com.dreambotreborn.api.methods.input.Keyboard.pressEsc();
    }
    public boolean isVisible()
    {
        for (WidgetChild child : getChildren()) if (child.isVisible()) return true;
        return false;
    }
    public boolean checkValidity(Object[] ignored) { return !getChildren().isEmpty(); }
    @Override public String toString() { return "Widget{" + id + ", children=" + getChildren().size() + '}'; }
}
