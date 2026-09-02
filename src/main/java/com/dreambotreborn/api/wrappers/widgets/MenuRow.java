package com.dreambotreborn.api.wrappers.widgets;

import java.util.Objects;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;

/** Mutable, client-independent menu entry snapshot. */
public class MenuRow implements Cloneable
{
    private int index;
    private String action;
    private String object;
    private int id;
    private int xCode;
    private int yCode;
    private int opCode;
    private int itemIdCode = -1;
    private int targetRegionIndex = net.runelite.api.WorldView.TOPLEVEL;
    private boolean shift;

    public MenuRow(String action, String object, int id, int xCode, int yCode,
                   int opCode, int itemIdCode)
    {
        this(0, action, object, id, xCode, yCode, opCode, itemIdCode,
            net.runelite.api.WorldView.TOPLEVEL, false);
    }

    public MenuRow(String action, String object, int id, int xCode, int yCode,
                   int opCode, int itemIdCode, int targetRegionIndex)
    {
        this(0, action, object, id, xCode, yCode, opCode, itemIdCode,
            targetRegionIndex, false);
    }

    public MenuRow(int index, String action, String object, int id, int xCode,
                   int yCode, int opCode, int itemIdCode, boolean shift)
    {
        this(index, action, object, id, xCode, yCode, opCode, itemIdCode,
            net.runelite.api.WorldView.TOPLEVEL, shift);
    }

    public MenuRow(int index, String action, String object, int id, int xCode,
                   int yCode, int opCode, int itemIdCode,
                   int targetRegionIndex, boolean shift)
    {
        this.index = index;
        this.action = clean(action);
        this.object = clean(object);
        this.id = id;
        this.xCode = xCode;
        this.yCode = yCode;
        this.opCode = opCode;
        this.itemIdCode = itemIdCode;
        this.targetRegionIndex = targetRegionIndex;
        this.shift = shift;
    }

    public MenuRow(MenuEntry entry, int index)
    {
        this(index, entry.getOption(), entry.getTarget(), entry.getIdentifier(),
            entry.getParam0(), entry.getParam1(), entry.getType().getId(),
            entry.getItemId(), entry.getWorldViewId(), entry.isDeprioritized());
    }

    public int getId() { return id; }
    public int getID() { return id; }
    public int getXCode() { return xCode; }
    public int getYCode() { return yCode; }
    public boolean compareAction(String value) { return equalsIgnoreCase(action, value); }
    public boolean compareObject(String value) { return equalsIgnoreCase(object, clean(value)); }
    public boolean compareEntity(Entity entity)
    {
        return entity != null && (id == entity.id || compareObject(entity.name));
    }

    @Override public MenuRow clone()
    {
        return new MenuRow(index, action, object, id, xCode, yCode, opCode,
            itemIdCode, targetRegionIndex, shift);
    }

    public void setIndex(int value) { index = value; }
    public int getIndex() { return index; }
    public void setAction(String value) { action = clean(value); }
    public String getAction() { return clean(action); }
    public String getRawAction() { return action; }
    public void setObject(String value) { object = clean(value); }
    public String getObject() { return clean(object); }
    public String getRawObject() { return object; }
    public void setId(int value) { id = value; }
    public void setXCode(int value) { xCode = value; }
    public void setYCode(int value) { yCode = value; }
    public void setOpCode(int value) { opCode = value; }
    public int getOpCode() { return opCode; }
    public void setItemIdCode(int value) { itemIdCode = value; }
    public int getItemIdCode() { return itemIdCode; }
    public void setTargetRegionIndex(int value) { targetRegionIndex = value; }
    public int getTargetRegionIndex() { return targetRegionIndex; }
    public void setShift(boolean value) { shift = value; }
    public boolean isShift() { return shift; }

    MenuEntry applyTo(MenuEntry entry)
    {
        return entry.setOption(action).setTarget(object).setIdentifier(id)
            .setParam0(xCode).setParam1(yCode).setType(MenuAction.of(opCode))
            .setItemId(itemIdCode).setWorldViewId(targetRegionIndex)
            .setDeprioritized(shift);
    }

    @Override public String toString() { return action + (object.isEmpty() ? "" : " " + object); }
    @Override public boolean equals(Object value)
    {
        if (this == value) return true;
        if (!(value instanceof MenuRow)) return false;
        MenuRow row = (MenuRow) value;
        return id == row.id && xCode == row.xCode && yCode == row.yCode
            && opCode == row.opCode && Objects.equals(action, row.action)
            && Objects.equals(object, row.object);
    }
    @Override public int hashCode() { return Objects.hash(action, object, id, xCode, yCode, opCode); }

    private static String clean(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]*>", "");
    }
    private static boolean equalsIgnoreCase(String first, String second)
    {
        return first != null && second != null && first.equalsIgnoreCase(second);
    }
}
