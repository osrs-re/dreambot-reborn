package com.dreambotreborn.api.wrappers.interactive;

/** Decorative wall attachment (RuneLite DecorativeObject). */
public class WallObject extends GameObject
{
    public WallObject(Object reference) { this(require(reference)); }
    public WallObject(net.runelite.api.DecorativeObject reference)
    {
        super(reference, definition(reference), Type.DECORATIVE);
    }
    private static net.runelite.api.DecorativeObject require(Object value)
    {
        if (!(value instanceof net.runelite.api.DecorativeObject))
            throw new IllegalArgumentException("reference must be a RuneLite DecorativeObject");
        return (net.runelite.api.DecorativeObject) value;
    }
    @Override public net.runelite.api.DecorativeObject unwrap()
    {
        return (net.runelite.api.DecorativeObject) super.unwrap();
    }
    public int getModelOffsetX() { return unwrap().getXOffset(); }
    public int getModelOffsetY() { return unwrap().getYOffset(); }
}
