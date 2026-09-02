package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;

public interface MenuRowListener extends EventListener
{
    default void onRowAdded(MenuEntryAdded event) { }
    default void onAction(MenuOptionClicked event) { }
}
