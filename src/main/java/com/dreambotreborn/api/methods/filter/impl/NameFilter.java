package com.dreambotreborn.api.methods.filter.impl;

import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.wrappers.interactive.Identifiable;

public final class NameFilter<E extends Identifiable> implements Filter<E>
{
    private final String[] names;

    public NameFilter(String... names)
    {
        this.names = names == null ? new String[0] : names.clone();
    }

    @Override
    public boolean match(E value)
    {
        return value != null && Queries.name(value.getName(), names);
    }
}
