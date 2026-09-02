package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;

public interface WidgetEventListener extends EventListener
{
    default void onWidgetLoaded(WidgetLoaded event) { }
    default void onWidgetClosed(WidgetClosed event) { }
}
