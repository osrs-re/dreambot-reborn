package com.dreambotreborn.api.wrappers.interactive;

public class SceneObject extends GameObject
{
    public SceneObject(Object reference)
    {
        this(require(reference));
    }
    public SceneObject(net.runelite.api.GameObject reference)
    {
        super(reference, definition(reference), Type.GAME);
    }
    private static net.runelite.api.GameObject require(Object value)
    {
        if (!(value instanceof net.runelite.api.GameObject))
            throw new IllegalArgumentException("reference must be a RuneLite GameObject");
        return (net.runelite.api.GameObject) value;
    }
    @Override public net.runelite.api.GameObject unwrap()
    {
        return (net.runelite.api.GameObject) super.unwrap();
    }
    public int getType() { return getFlags() & 31; }
    public int getRelativeX() { return unwrap().getSceneMinLocation().getX(); }
    public int getRelativeY() { return unwrap().getSceneMinLocation().getY(); }
}
