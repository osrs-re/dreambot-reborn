package com.dreambotreborn.api.wrappers.interactive;

/** Boundary/wall object backed by RuneLite's WallObject. */
public class BoundaryObject extends GameObject
{
    public BoundaryObject(Object reference) { this(require(reference)); }
    public BoundaryObject(net.runelite.api.WallObject reference)
    {
        super(reference, definition(reference), Type.WALL);
    }
    private static net.runelite.api.WallObject require(Object value)
    {
        if (!(value instanceof net.runelite.api.WallObject))
            throw new IllegalArgumentException("reference must be a RuneLite WallObject");
        return (net.runelite.api.WallObject) value;
    }
    @Override public net.runelite.api.WallObject unwrap()
    {
        return (net.runelite.api.WallObject) super.unwrap();
    }
    @Override public int getOrientation() { return unwrap().getOrientationA(); }
    public int getBackUpOrientation() { return unwrap().getOrientationB(); }
}
