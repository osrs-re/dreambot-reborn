package com.dreambotreborn.api.wrappers.widgets;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import net.runelite.api.MenuEntry;

/** DreamBot-style facade over RuneLite's live context menu. */
public final class Menu
{
    private static volatile boolean manipulationActive;

    private Menu() { }

    public static boolean containsEntity(Entity entity)
    {
        for (MenuRow row : getMenuRows()) if (row.compareEntity(entity)) return true;
        return false;
    }
    public static boolean isVisible() { return DreamBotRebornApi.requireClient().isMenuOpen(); }
    public static int getHeight() { return DreamBotRebornApi.requireClient().getMenu().getMenuHeight(); }
    public static int getWidth() { return DreamBotRebornApi.requireClient().getMenu().getMenuWidth(); }
    public static int getY() { return DreamBotRebornApi.requireClient().getMenu().getMenuY(); }
    public static int getX() { return DreamBotRebornApi.requireClient().getMenu().getMenuX(); }
    public static int getCount() { return entries().length; }
    public static String[] getOptions()
    {
        List<MenuRow> rows = getMenuRows();
        String[] result = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) result[i] = rows.get(i).getAction();
        return result;
    }
    public static List<GameMenu> getSubMenus()
    {
        List<GameMenu> result = new ArrayList<>();
        for (MenuEntry entry : entries())
            if (entry.getSubMenu() != null) result.add(new GameMenu(entry.getSubMenu(), result.size()));
        return Collections.unmodifiableList(result);
    }
    public static GameMenu getMenu() { return new GameMenu(DreamBotRebornApi.requireClient().getMenu(), 0); }
    public static MenuRow getFirstMenuRow()
    {
        List<MenuRow> rows = getMenuRows();
        return rows.isEmpty() ? null : rows.get(0);
    }
    public static List<MenuRow> getMenuRows()
    {
        MenuEntry[] entries = entries();
        List<MenuRow> rows = new ArrayList<>(entries.length);
        for (int index = entries.length - 1; index >= 0; index--)
            rows.add(new MenuRow(entries[index], entries.length - 1 - index));
        return Collections.unmodifiableList(rows);
    }
    public static Rectangle getBounds() { return new Rectangle(getX(), getY(), getWidth(), getHeight()); }
    public static String getDefaultAction()
    {
        MenuRow row = getFirstMenuRow();
        return row == null ? null : row.getAction();
    }
    public static int getIndex(String action) { return getIndex(action, null, null); }
    public static int getIndex(String action, Entity entity) { return getIndex(action, null, entity); }
    public static int getIndex(String action, String object) { return getIndex(action, object, null); }
    public static int getIndex(String action, String object, int id)
    {
        List<MenuRow> rows = getMenuRows();
        for (int i = 0; i < rows.size(); i++)
        {
            MenuRow row = rows.get(i);
            if (matches(row, action, object, null) && row.getId() == id) return i;
        }
        return -1;
    }
    public static int getIndex(String action, String object, Entity entity)
    {
        List<MenuRow> rows = getMenuRows();
        for (int i = 0; i < rows.size(); i++)
            if (matches(rows.get(i), action, object, entity)) return i;
        return -1;
    }
    public static boolean clickAction(String action) { return clickIndex(getIndex(action)); }
    public static boolean clickAction(String action, String object)
    {
        return clickIndex(getIndex(action, object));
    }
    public static boolean clickAction(String action, Entity entity)
    {
        return clickIndex(getIndex(action, entity));
    }
    public static boolean clickAction(String action, String object, Entity entity)
    {
        return clickIndex(getIndex(action, object, entity));
    }
    public static boolean clickAction(String action, String object, int id)
    {
        return clickIndex(getIndex(action, object, id));
    }
    public static boolean clickIndex(int index)
    {
        Rectangle bounds = getIndexRectangle(index);
        return bounds != null && Mouse.click(bounds);
    }
    public static boolean open() { return isVisible() || Mouse.click(true); }
    public static Rectangle getIndexRectangle(int index)
    {
        if (index < 0 || index >= getCount() || !isVisible()) return null;
        int header = Math.max(19, getHeight() - getCount() * 15);
        return new Rectangle(getX() + 2, getY() + header + index * 15,
            Math.max(1, getWidth() - 4), 15);
    }
    public static boolean mouseOverAction(String action) { return mouseOverAction(getIndex(action)); }
    public static boolean mouseOverAction(int index)
    {
        Rectangle bounds = getIndexRectangle(index);
        return bounds != null && Mouse.move(bounds);
    }
    public static boolean isMouseOnAction(String action) { return isMouseOnAction(getIndex(action)); }
    public static boolean isMouseOnAction(int index)
    {
        Rectangle bounds = getIndexRectangle(index);
        return bounds != null && bounds.contains(Mouse.getPosition());
    }
    public static boolean close()
    {
        if (!isVisible()) return true;
        Rectangle bounds = getBounds();
        Point point = bounds.x > 5 ? new Point(bounds.x - 3, bounds.y) :
            new Point(bounds.x + bounds.width + 3, bounds.y);
        return Mouse.click(point);
    }
    public static boolean containsObject(String object) { return getIndex(null, object, null) >= 0; }
    public static boolean contains(String action) { return getIndex(action) >= 0; }
    public static boolean contains(String action, Entity entity) { return getIndex(action, entity) >= 0; }
    public static boolean contains(String action, String object, Entity entity)
    {
        return getIndex(action, object, entity) >= 0;
    }
    public static boolean isMenuManipulationActive() { return manipulationActive; }
    public static void toggleMenuManipulation(boolean value) { manipulationActive = value; }
    public static boolean inject(MenuRow row) { return inject(row, null); }
    public static boolean inject(MenuRow row, Point point)
    {
        if (row == null) return false;
        return Await.result(ClientThread.invoke(() ->
        {
            net.runelite.api.Menu menu = DreamBotRebornApi.requireClient().getMenu();
            row.applyTo(menu.createMenuEntry(-1));
            return true;
        }), false) && (point == null || Mouse.move(point));
    }
    public static boolean hover(int index) { return mouseOverAction(index); }

    private static MenuEntry[] entries()
    {
        MenuEntry[] values = DreamBotRebornApi.requireClient().getMenu().getMenuEntries();
        return values == null ? new MenuEntry[0] : values;
    }
    private static boolean matches(MenuRow row, String action, String object, Entity entity)
    {
        return (action == null || row.compareAction(action))
            && (object == null || row.compareObject(object))
            && (entity == null || row.compareEntity(entity));
    }
}
