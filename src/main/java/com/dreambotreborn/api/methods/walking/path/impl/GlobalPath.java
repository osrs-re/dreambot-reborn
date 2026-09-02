package com.dreambotreborn.api.methods.walking.path.impl;

import java.util.Collection;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.methods.walking.path.AbstractPath;
import com.dreambotreborn.api.methods.walking.web.node.AbstractWebNode;
import com.dreambotreborn.api.methods.walking.web.node.WebNodeType;

public class GlobalPath<T extends AbstractWebNode> extends AbstractPath<T>
{
    private static volatile GlobalPath<?> lastWalkedPath;
    private int finalCost;
    public GlobalPath() { }
    public GlobalPath(Collection<? extends T> nodes) { super(nodes); }
    @Override public boolean walk()
    {
        lastWalkedPath = this;
        T next = next();
        if (next != null && next.forceNext()) return next.execute();
        return Walking.walkPath(this);
    }
    public boolean hasSpecialNode()
    {
        for (T node : this) if (node != null && node.getType() != WebNodeType.BASIC_NODE) return true;
        return false;
    }
    @Override public T next()
    {
        for (int index = size() - 1; index >= 0; index--)
            if (Walking.canWalk(get(index).getTile())) return get(index);
        return first();
    }
    public T closest() { return closest(null); }
    public T closest(Tile origin)
    {
        T result = null;
        double distance = Double.POSITIVE_INFINITY;
        for (T node : this)
        {
            double candidate = origin == null ? node.getTile().distance() : node.getTile().distance(origin);
            if (candidate < distance) { result = node; distance = candidate; }
        }
        return result;
    }
    public static GlobalPath<?> getLastWalkedPath() { return lastWalkedPath; }
    public int getFinalCost() { return finalCost; }
    public void setFinalCost(int value) { finalCost = value; }
}
