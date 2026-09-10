package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;

public interface MenuRowListener extends EventListener
{
    default void onRowAdded(MenuRow row) { }

    /** RuneLite-native callbacks retained for lower-level integrations. */
    default void onRowAdded(MenuEntryAdded event) { }
    default void onAction(MenuOptionClicked event) { }
}
