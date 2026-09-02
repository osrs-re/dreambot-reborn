package com.dreambotreborn.api.methods.container.impl;

import java.util.Comparator;
import com.dreambotreborn.api.wrappers.items.Item;

/** Controls the order used by {@link Inventory#dropAll()} operations. */
@FunctionalInterface
public interface DropPattern
{
    Comparator<Item> getComparator();

    DropPattern SLOT_ORDER = () -> Comparator.comparingInt(Item::getSlot);
}
