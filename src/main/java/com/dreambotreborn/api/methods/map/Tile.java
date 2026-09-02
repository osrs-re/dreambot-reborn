package com.dreambotreborn.api.methods.map;

import java.awt.Polygon;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Locatable;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;

/** DreamBot-style immutable-friendly world tile wrapper. */
public class Tile implements Locatable, Cloneable
{
    private int x;
    private int y;
    private int z;

    public Tile()
    {
    }

    public Tile(int x, int y)
    {
        this(x, y, 0);
    }

    public Tile(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Tile(WorldPoint point)
    {
        this(point.getX(), point.getY(), point.getPlane());
    }

    public Tile translate(Tile offset)
    {
        return offset == null ? clone() : translate(offset.x, offset.y);
    }

    public Tile translate(int deltaX, int deltaY)
    {
        return new Tile(x + deltaX, y + deltaY, z);
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }

    public int getGridX()
    {
        return x - DreamBotRebornApi.requireClient().getBaseX();
    }

    public int getGridY()
    {
        return y - DreamBotRebornApi.requireClient().getBaseY();
    }

    public double distance(Entity entity)
    {
        return entity == null ? Double.POSITIVE_INFINITY : distance(entity.getTile());
    }

    public double distance(Tile other)
    {
        if (other == null || other.z != z)
        {
            return Double.POSITIVE_INFINITY;
        }
        return Math.max(Math.abs(x - other.x), Math.abs(y - other.y));
    }

    public double distance()
    {
        net.runelite.api.Player player = DreamBotRebornApi.requireClient().getLocalPlayer();
        return player == null ? Double.POSITIVE_INFINITY : distance(new Tile(player.getWorldLocation()));
    }

    public double walkingDistance(Tile other)
    {
        return distance(other);
    }

    public Polygon getPolygon()
    {
        LocalPoint local = LocalPoint.fromWorld(DreamBotRebornApi.requireClient(), toWorldPoint());
        return local == null ? null : Perspective.getCanvasTilePoly(DreamBotRebornApi.requireClient(), local);
    }

    public Tile getRandomizedTile()
    {
        return getRandomizedTile(1);
    }

    public Tile getRandomized()
    {
        return getRandomizedTile();
    }

    public Tile getRandomizedTile(int radius)
    {
        int safeRadius = Math.max(0, radius);
        return new Tile(
            x + ThreadLocalRandom.current().nextInt(-safeRadius, safeRadius + 1),
            y + ThreadLocalRandom.current().nextInt(-safeRadius, safeRadius + 1), z);
    }

    public Tile getRandomized(int radius)
    {
        return getRandomizedTile(radius);
    }

    public Area getArea(int radius)
    {
        return new Area(x - radius, y - radius, x + radius, y + radius, z);
    }

    public WorldPoint toWorldPoint()
    {
        return new WorldPoint(x, y, z);
    }

    @Override
    public Tile getTile()
    {
        return this;
    }

    @Override
    public Tile clone()
    {
        return new Tile(x, y, z);
    }

    @Override
    public boolean equals(Object value)
    {
        if (this == value) return true;
        if (!(value instanceof Tile)) return false;
        Tile tile = (Tile) value;
        return x == tile.x && y == tile.y && z == tile.z;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString()
    {
        return "Tile{" + x + ", " + y + ", " + z + '}';
    }
}
