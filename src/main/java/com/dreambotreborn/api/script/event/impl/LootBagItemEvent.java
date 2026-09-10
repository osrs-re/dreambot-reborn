package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.listener.ItemContainerListener;
import com.dreambotreborn.api.wrappers.items.Item;

public class LootBagItemEvent extends ContainerItemEvent
{
    public LootBagItemEvent(Item previous, Item current) { super(previous, current); }
    public LootBagItemEvent(Item item) { super(item); }
    @Override public final void dispatch(EventListener listener)
    {
        if (!(listener instanceof ItemContainerListener)) return;
        ItemContainerListener target = (ItemContainerListener) listener;
        if (added()) target.onLootBagItemAdded(current);
        else if (removed()) target.onLootBagItemRemoved(previous);
        else target.onLootBagItemUpdated(previous, current);
    }
}
