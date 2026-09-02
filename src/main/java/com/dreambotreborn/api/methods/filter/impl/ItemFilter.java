package com.dreambotreborn.api.methods.filter.impl;

import java.util.Objects;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.wrappers.items.Item;

public final class ItemFilter<E extends Item> implements Filter<E>
{
    private final E expected;

    public ItemFilter(E expected)
    {
        this.expected = Objects.requireNonNull(expected, "expected");
    }

    @Override
    public boolean match(E value)
    {
        return value != null && value.getId() == expected.getId()
            && value.getContainerType() == expected.getContainerType()
            && value.getSlot() == expected.getSlot();
    }
}
