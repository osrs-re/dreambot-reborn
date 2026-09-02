package com.dreambotreborn.api.wrappers.widgets.builder;

import java.util.concurrent.ConcurrentLinkedQueue;
import com.dreambotreborn.api.wrappers.widgets.Menu;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;

public final class MenuRowQueue
{
    private static final ConcurrentLinkedQueue<MenuRow> ROWS = new ConcurrentLinkedQueue<>();
    private MenuRowQueue() { }
    public static void queue(MenuRow... rows)
    {
        if (rows != null) for (MenuRow row : rows) if (row != null) ROWS.add(row);
    }
    public static boolean enabled() { return !ROWS.isEmpty(); }
    public static void flush()
    {
        MenuRow row;
        while ((row = ROWS.poll()) != null) Menu.inject(row);
    }
}
