package com.dreambotreborn.api.methods.filter.impl;

import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.wrappers.interactive.Identifiable;

public final class IdFilter<E extends Identifiable> implements Filter<E>
{
    private final int[] ids;

    public IdFilter(Integer... ids)
    {
        if (ids == null)
        {
            this.ids = new int[0];
            return;
        }
        this.ids = new int[ids.length];
        for (int index = 0; index < ids.length; index++)
            this.ids[index] = ids[index] == null ? Integer.MIN_VALUE : ids[index];
    }

    public IdFilter(int[] ids)
    {
        this.ids = ids == null ? new int[0] : ids.clone();
    }

    @Override
    public boolean match(E value)
    {
        if (value == null) return false;
        for (int id : ids) if (value.getId() == id) return true;
        return false;
    }
}
