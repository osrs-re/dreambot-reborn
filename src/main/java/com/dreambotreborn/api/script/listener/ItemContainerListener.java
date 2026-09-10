package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.items.Item;
import net.runelite.api.events.ItemContainerChanged;

public interface ItemContainerListener extends EventListener
{
    default void onInventoryItemChanged(Item previous, Item current) { }
    default void onInventoryItemAdded(Item item) { }
    default void onInventoryItemRemoved(Item item) { }
    default void onInventoryItemSwapped(Item first, Item second) { }
    default void onEquipmentItemChanged(Item previous, Item current) { }
    default void onEquipmentItemAdded(Item item) { }
    default void onEquipmentItemRemoved(Item item) { }
    default void onEquipmentItemSwapped(Item first, Item second) { }
    default void onBankItemChanged(Item previous, Item current) { }
    default void onBankItemAdded(Item item) { }
    default void onBankItemRemoved(Item item) { }
    default void onBankItemSwapped(Item first, Item second) { }
    default void onLootBagItemAdded(Item item) { }
    default void onLootBagItemRemoved(Item item) { }
    default void onLootBagItemUpdated(Item previous, Item current) { }

    /** RuneLite-native callback retained for scripts that need the original event. */
    default void onItemContainerChanged(ItemContainerChanged event) { }
}
