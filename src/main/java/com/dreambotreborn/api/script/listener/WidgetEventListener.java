package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;

public interface WidgetEventListener extends EventListener
{
    default void onWidgetDecoded(WidgetChild widget) { }
    default void onWidgetLoaded(WidgetChild widget) { }

    /** RuneLite-native callbacks retained for lower-level integrations. */
    default void onWidgetLoaded(WidgetLoaded event) { }
    default void onWidgetClosed(WidgetClosed event) { }
}
