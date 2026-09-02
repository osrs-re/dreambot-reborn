package com.dreambotreborn.api.wrappers.graphics;

import java.util.Objects;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.interactive.Locatable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/** Immutable view of a live scene graphics effect. */
public final class GraphicsObject implements Locatable
{
    private final net.runelite.api.GraphicsObject value;
    private final Tile tile;

    public GraphicsObject(net.runelite.api.GraphicsObject value)
    {
        this.value = Objects.requireNonNull(value, "value");
        LocalPoint local = value.getLocation();
        WorldPoint world = local == null || value.getWorldView() == null ? null
            : WorldPoint.fromLocal(value.getWorldView(), local.getX(), local.getY(), value.getLevel());
        tile = world == null ? null : new Tile(world);
    }

    public int getId() { return value.getId(); }
    public int getID() { return getId(); }
    public int getX() { return tile == null ? -1 : tile.getX(); }
    public int getY() { return tile == null ? -1 : tile.getY(); }
    public int getZ() { return value.getZ(); }
    public int getSpawnTime() { return value.getStartCycle(); }
    public boolean isFinished() { return value.finished(); }
    @Override public Tile getTile() { return tile; }
    public net.runelite.api.GraphicsObject unwrap() { return value; }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof GraphicsObject && ((GraphicsObject) other).value == value;
    }

    @Override public int hashCode() { return System.identityHashCode(value); }
    @Override public String toString() { return "GraphicsObject{" + getId() + ", " + tile + '}'; }
}
