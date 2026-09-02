package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.ItemContainerChanged;

public interface ItemContainerListener extends EventListener
{
    default void onItemContainerChanged(ItemContainerChanged event) { }
}
