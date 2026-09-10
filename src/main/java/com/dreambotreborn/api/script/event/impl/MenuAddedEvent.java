package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.MenuRowListener;
import com.dreambotreborn.api.wrappers.widgets.Menu;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;

public class MenuAddedEvent extends ScriptEvent
{
    private final MenuRow row;
    public MenuAddedEvent() { this(Menu.getFirstMenuRow()); }
    public MenuAddedEvent(MenuRow row) { this.row = row; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof MenuRowListener && row != null)
            ((MenuRowListener) listener).onRowAdded(row);
    }
    public MenuRow getMenuRow() { return row; }
    @Override public String toString() { return "MenuAddedEvent{" + row + '}'; }
}
