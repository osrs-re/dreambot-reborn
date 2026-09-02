package com.dreambotreborn.api.methods.filter.impl;

import java.util.Objects;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.methods.map.Area;
import com.dreambotreborn.api.wrappers.interactive.Locatable;

public final class AreaFilter<E extends Locatable> implements Filter<E>
{
    private final Area area;

    public AreaFilter(Area area)
    {
        this.area = Objects.requireNonNull(area, "area");
    }

    @Override
    public boolean match(E value)
    {
        return value != null && area.contains(value.getTile());
    }
}
