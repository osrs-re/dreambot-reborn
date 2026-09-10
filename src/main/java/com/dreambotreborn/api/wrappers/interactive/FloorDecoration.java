package com.dreambotreborn.api.wrappers.interactive;

public class FloorDecoration extends GameObject
{
    public FloorDecoration(Object reference) { this(require(reference)); }
    public FloorDecoration(net.runelite.api.GroundObject reference)
    {
        super(reference, definition(reference), Type.GROUND);
    }
    private static net.runelite.api.GroundObject require(Object value)
    {
        if (!(value instanceof net.runelite.api.GroundObject))
            throw new IllegalArgumentException("reference must be a RuneLite GroundObject");
        return (net.runelite.api.GroundObject) value;
    }
    @Override public net.runelite.api.GroundObject unwrap()
    {
        return (net.runelite.api.GroundObject) super.unwrap();
    }
}
