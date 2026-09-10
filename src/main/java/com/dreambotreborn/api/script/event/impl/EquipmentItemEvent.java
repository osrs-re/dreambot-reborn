package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.listener.ItemContainerListener;
import com.dreambotreborn.api.wrappers.items.Item;

public class EquipmentItemEvent extends ContainerItemEvent
{
    public EquipmentItemEvent(Item item) { super(item); }
    public EquipmentItemEvent(Item previous, Item current) { super(previous, current); }
    @Override public final void dispatch(EventListener listener)
    {
        if (!(listener instanceof ItemContainerListener)) return;
        ItemContainerListener target = (ItemContainerListener) listener;
        if (added()) target.onEquipmentItemAdded(current);
        else if (removed()) target.onEquipmentItemRemoved(previous);
        else if (swapped()) target.onEquipmentItemSwapped(previous, current);
        else target.onEquipmentItemChanged(previous, current);
    }
}
