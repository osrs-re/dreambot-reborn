package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.listener.ItemContainerListener;
import com.dreambotreborn.api.wrappers.items.Item;

public class BankItemEvent extends ContainerItemEvent
{
    public BankItemEvent(Item item) { super(item); }
    public BankItemEvent(Item previous, Item current) { super(previous, current); }
    @Override public final void dispatch(EventListener listener)
    {
        if (!(listener instanceof ItemContainerListener)) return;
        ItemContainerListener target = (ItemContainerListener) listener;
        if (added()) target.onBankItemAdded(current);
        else if (removed()) target.onBankItemRemoved(previous);
        else if (swapped()) target.onBankItemSwapped(previous, current);
        else target.onBankItemChanged(previous, current);
    }
}
