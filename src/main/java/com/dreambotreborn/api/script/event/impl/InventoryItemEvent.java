package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.listener.ItemContainerListener;
import com.dreambotreborn.api.wrappers.items.Item;

public class InventoryItemEvent extends ContainerItemEvent
{
    public InventoryItemEvent(Item item) { super(item); }
    public InventoryItemEvent(Item previous, Item current) { super(previous, current); }
    @Override public final void dispatch(EventListener listener)
    {
        if (!(listener instanceof ItemContainerListener)) return;
        ItemContainerListener target = (ItemContainerListener) listener;
        if (added()) target.onInventoryItemAdded(current);
        else if (removed()) target.onInventoryItemRemoved(previous);
        else if (swapped()) target.onInventoryItemSwapped(previous, current);
        else target.onInventoryItemChanged(previous, current);
    }
}
