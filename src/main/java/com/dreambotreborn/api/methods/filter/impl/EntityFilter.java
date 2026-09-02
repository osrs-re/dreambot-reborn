package com.dreambotreborn.api.methods.filter.impl;

import java.util.Objects;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.wrappers.interactive.Entity;

public final class EntityFilter<E extends Entity> implements Filter<E>
{
    private final E expected;

    public EntityFilter(E expected)
    {
        this.expected = Objects.requireNonNull(expected, "expected");
    }

    @Override
    public boolean match(E value)
    {
        return value != null && value.getId() == expected.getId()
            && Objects.equals(value.getTile(), expected.getTile())
            && Objects.equals(value.getName(), expected.getName());
    }
}
