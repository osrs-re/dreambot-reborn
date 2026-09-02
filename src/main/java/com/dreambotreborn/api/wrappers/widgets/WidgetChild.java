package com.dreambotreborn.api.wrappers.widgets;

import java.awt.Rectangle;
import com.dreambotreborn.api.input.Mouse;

/** DreamBot-named widget child backed by a live RuneLite widget. */
public class WidgetChild extends Widget
{
    public WidgetChild(net.runelite.api.widgets.Widget widget) { super(widget); }

    public void updateReference(Object reference)
    {
        if (!(reference instanceof net.runelite.api.widgets.Widget))
            throw new IllegalArgumentException("reference must be a RuneLite widget");
    }
    public int getModelRotationX() { return unwrap().getRotationX(); }
    public int getModelRotationY() { return unwrap().getRotationY(); }
    public int getModelRotationZ() { return unwrap().getRotationZ(); }
    public int getSequenceId() { return unwrap().getAnimationId(); }
    public int getScrollMax() { return unwrap().getScrollHeight(); }
    public int getScrollX() { return unwrap().getScrollX(); }
    public int getScrollY() { return unwrap().getScrollY(); }
    public int getTextColor() { return unwrap().getTextColor(); }
    public int getContentType() { return unwrap().getContentType(); }
    public int getDragDeadTime() { return unwrap().getDragDeadTime(); }
    public void setDragDeadTime(int value) { unwrap().setDragDeadTime(value); }
    public boolean hasListener() { return unwrap().hasListener(); }
    public void setHasListener(boolean value) { unwrap().setHasListener(value); }
    public String[][] getSubActions() { return unwrap().getSubOps(); }
    public int getHeight() { return unwrap().getHeight(); }
    public int getWidth() { return unwrap().getWidth(); }
    public int getX() { return bounds == null ? -1 : bounds.x; }
    public int getY() { return bounds == null ? -1 : bounds.y; }
    public int getID() { return id; }
    public int getRealID() { return id; }
    public int getRawId() { return id; }
    public int getWidgetId() { return groupId; }
    public int getRealParentID() { return parentId; }
    public int getParentID() { return parentId; }
    public int getGrandChildId() { return index; }
    public int getRelativeX() { return unwrap().getRelativeX(); }
    public int getRelativeY() { return unwrap().getRelativeY(); }
    public boolean isHidden() { return unwrap().isHidden(); }
    public int getItemStack() { return itemQuantity; }
    public void setItemId(int value) { unwrap().setItemId(value); }
    public void setItemStack(int value) { unwrap().setItemQuantity(value); }
    public void setText(String value) { unwrap().setText(value); }
    public Rectangle getRectangle() { return getBounds(); }
    public boolean isGrandChild() { return index >= 0; }
    public boolean hasActionExact(String action) { return hasAction(action); }
    public String getLeftClickAction()
    {
        int index = firstActionIndex();
        return actionAt(index);
    }
    public boolean containsMouse()
    {
        Rectangle rectangle = getBounds();
        return rectangle != null && rectangle.contains(Mouse.getPosition());
    }
    public Object getReference() { return unwrap(); }
    public int getChildrenCount() { return getChildren().length; }
    public WidgetChild getChild(int child)
    {
        net.runelite.api.widgets.Widget value = unwrap().getChild(child);
        return value == null ? null : new WidgetChild(value);
    }
    public WidgetChild[] getChildren()
    {
        net.runelite.api.widgets.Widget[] values = unwrap().getChildren();
        if (values == null) return new WidgetChild[0];
        java.util.List<WidgetChild> result = new java.util.ArrayList<>();
        for (net.runelite.api.widgets.Widget value : values)
            if (value != null) result.add(new WidgetChild(value));
        return result.toArray(new WidgetChild[0]);
    }
    public WidgetChild getSibling(int offset)
    {
        net.runelite.api.widgets.Widget parent = unwrap().getParent();
        net.runelite.api.widgets.Widget value = parent == null ? null : parent.getChild(index + offset);
        return value == null ? null : new WidgetChild(value);
    }
    public boolean hasSubAction(String action) { return getSubActionIndex(action) >= 0; }
    public int getSubActionIndex(String action)
    {
        String[][] values = getSubActions();
        if (values != null)
            for (int row = 0; row < values.length; row++)
                if (values[row] != null)
                    for (String value : values[row])
                        if (value != null && value.equalsIgnoreCase(action)) return row;
        return -1;
    }
}
