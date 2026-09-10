package com.dreambotreborn.api.wrappers.items;

import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.cache.nodes.RenderableNode;

public class ItemLayer
{
    private final net.runelite.api.ItemLayer reference;
    public ItemLayer(Object reference)
    {
        if (!(reference instanceof net.runelite.api.ItemLayer))
            throw new IllegalArgumentException("reference must be a RuneLite ItemLayer");
        this.reference = (net.runelite.api.ItemLayer) reference;
    }
    public net.runelite.api.ItemLayer getReference() { return reference; }
    public RenderableNode getTop() { return wrap(reference.getTop()); }
    public RenderableNode getMiddle() { return wrap(reference.getMiddle()); }
    public RenderableNode getBottom() { return wrap(reference.getBottom()); }
    public int getX() { return reference.getWorldLocation().getX(); }
    public int getY() { return reference.getWorldLocation().getY(); }
    public int getHeight() { return reference.getHeight(); }
    public int getPlane() { return reference.getPlane(); }
    public Tile getTile() { return new Tile(reference.getWorldLocation()); }
    private static RenderableNode wrap(net.runelite.api.Renderable value)
    {
        return value == null ? null : new RenderableNode(value);
    }
}
