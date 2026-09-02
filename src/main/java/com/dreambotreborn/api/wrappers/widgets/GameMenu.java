package com.dreambotreborn.api.wrappers.widgets;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import net.runelite.api.MenuEntry;

/** Instance wrapper used for root menus and RuneLite submenus. */
public class GameMenu
{
    private final net.runelite.api.Menu menu;
    private final int subIndex;

    public GameMenu(net.runelite.api.Menu menu, int subIndex)
    {
        this.menu = menu;
        this.subIndex = subIndex;
    }

    public boolean containsEntity(Entity entity)
    {
        for (MenuRow row : getMenuRows()) if (row.compareEntity(entity)) return true;
        return false;
    }
    public int getLastHovered() { return -1; }
    public boolean isVisible() { return menu != null && getCount() > 0; }
    public int getHeight() { return menu == null ? 0 : menu.getMenuHeight(); }
    public int getWidth() { return menu == null ? 0 : menu.getMenuWidth(); }
    public int getY() { return menu == null ? 0 : menu.getMenuY(); }
    public int getX() { return menu == null ? 0 : menu.getMenuX(); }
    public int getCount() { return entries().length; }
    public String[] getOptions()
    {
        List<MenuRow> rows = getMenuRows();
        String[] result = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) result[i] = rows.get(i).getAction();
        return result;
    }
    public List<GameMenu> getSubMenu()
    {
        List<GameMenu> result = new ArrayList<>();
        for (MenuEntry entry : entries())
            if (entry.getSubMenu() != null) result.add(new GameMenu(entry.getSubMenu(), result.size()));
        return Collections.unmodifiableList(result);
    }
    public GameMenu getMenuWithAction(String action)
    {
        if (getIndex(action) >= 0) return this;
        for (GameMenu child : getSubMenu())
        {
            GameMenu result = child.getMenuWithAction(action);
            if (result != null) return result;
        }
        return null;
    }
    public MenuRow getFirstMenuRow()
    {
        List<MenuRow> rows = getMenuRows();
        return rows.isEmpty() ? null : rows.get(0);
    }
    public List<MenuRow> getMenuRows()
    {
        MenuEntry[] entries = entries();
        List<MenuRow> rows = new ArrayList<>();
        for (int i = entries.length - 1; i >= 0; i--)
            rows.add(new MenuRow(entries[i], entries.length - 1 - i));
        return Collections.unmodifiableList(rows);
    }
    public Rectangle getBounds() { return new Rectangle(getX(), getY(), getWidth(), getHeight()); }
    public String getDefaultAction()
    {
        MenuRow row = getFirstMenuRow();
        return row == null ? null : row.getAction();
    }
    public int getIndex(String action) { return getIndex(action, null, -1); }
    public int getIndex(String action, Entity entity)
    {
        List<MenuRow> rows = getMenuRows();
        for (int i = 0; i < rows.size(); i++)
            if (rows.get(i).compareAction(action) && rows.get(i).compareEntity(entity)) return i;
        return -1;
    }
    public int getIndex(String action, String object) { return getIndex(action, object, -1); }
    public int getIndex(String action, String object, int id)
    {
        List<MenuRow> rows = getMenuRows();
        for (int i = 0; i < rows.size(); i++)
        {
            MenuRow row = rows.get(i);
            if ((action == null || row.compareAction(action))
                && (object == null || row.compareObject(object))
                && (id < 0 || row.getId() == id)) return i;
        }
        return -1;
    }
    public boolean clickAction(String action) { return clickIndex(getIndex(action)); }
    public boolean clickAction(String action, String object) { return clickIndex(getIndex(action, object)); }
    public boolean clickAction(String action, Entity entity) { return clickIndex(getIndex(action, entity)); }
    public boolean clickAction(String action, String object, int id)
    {
        return clickIndex(getIndex(action, object, id));
    }
    public boolean clickIndex(int index)
    {
        Rectangle row = getIndexRectangle(index);
        return row != null && Mouse.click(row);
    }
    public boolean open() { return isVisible() || Mouse.click(true); }
    public Rectangle getIndexRectangle(int index)
    {
        if (index < 0 || index >= getCount()) return null;
        int header = Math.max(19, getHeight() - getCount() * 15);
        return new Rectangle(getX() + 2, getY() + header + index * 15,
            Math.max(1, getWidth() - 4), 15);
    }
    public boolean mouseOverAction(String action) { return mouseOverAction(getIndex(action)); }
    public boolean mouseOverAction(int index)
    {
        Rectangle row = getIndexRectangle(index);
        return row != null && Mouse.move(row);
    }
    public boolean isMouseOnAction(String action) { return isMouseOnAction(getIndex(action)); }
    public boolean isMouseOnAction(int index)
    {
        Rectangle row = getIndexRectangle(index);
        return row != null && row.contains(Mouse.getPosition());
    }
    public boolean close() { return Menu.close(); }
    public boolean containsObject(String object) { return getIndex(null, object) >= 0; }
    public boolean contains(String action) { return getIndex(action) >= 0; }
    public boolean contains(String action, Entity entity) { return getIndex(action, entity) >= 0; }
    public boolean hover(int index) { return mouseOverAction(index); }
    public int getSubIndex() { return subIndex; }

    private MenuEntry[] entries()
    {
        if (menu == null || menu.getMenuEntries() == null) return new MenuEntry[0];
        return menu.getMenuEntries();
    }
}
