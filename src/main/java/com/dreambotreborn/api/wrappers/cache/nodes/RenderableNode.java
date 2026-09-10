package com.dreambotreborn.api.wrappers.cache.nodes;

/** Lightweight compatibility wrapper for a RuneLite renderable. */
public class RenderableNode
{
    private final Object reference;
    public RenderableNode(Object reference) { this.reference = reference; }
    public int getHeight()
    {
        return reference instanceof net.runelite.api.Renderable
            ? ((net.runelite.api.Renderable) reference).getModelHeight() : 0;
    }
    public Object getReference() { return reference; }
}
