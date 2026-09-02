package com.dreambotreborn.api.methods.walking.path.impl;

import java.util.Collection;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.methods.walking.path.AbstractPath;
import com.dreambotreborn.api.wrappers.interactive.Locatable;

public class LocalPath<T extends Locatable> extends AbstractPath<T>
{
    private static volatile LocalPath<?> lastWalkedPath;
    public LocalPath() { }
    public LocalPath(Collection<? extends T> values) { super(values); }
    @Override public boolean walk()
    {
        lastWalkedPath = this;
        return Walking.walkPath(this);
    }
    @Override public T next() { return next(0); }
    public T next(int minimumDistance)
    {
        for (int index = size() - 1; index >= 0; index--)
        {
            T value = get(index);
            Tile tile = value == null ? null : value.getTile();
            if (tile != null && tile.distance() >= minimumDistance && Walking.canWalk(tile)) return value;
        }
        return first();
    }
    public T getFurthestOnMM() { return next(); }
    public T closest() { return closest(null); }
    public T closest(Tile origin)
    {
        T result = null;
        double distance = Double.POSITIVE_INFINITY;
        for (T value : this)
        {
            if (value == null || value.getTile() == null) continue;
            double candidate = origin == null ? value.getTile().distance() : value.getTile().distance(origin);
            if (candidate < distance) { result = value; distance = candidate; }
        }
        return result;
    }
    public static LocalPath<?> getLastWalkedPath() { return lastWalkedPath; }
}
