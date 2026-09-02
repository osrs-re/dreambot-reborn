package com.dreambotreborn.api.wrappers.graphics;

import java.util.Objects;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.interactive.Locatable;
import net.runelite.api.Actor;
import net.runelite.api.coords.WorldPoint;

/** Immutable-friendly projectile wrapper retaining access to its live actor targets. */
public final class Projectile implements Locatable
{
    private final net.runelite.api.Projectile value;

    public Projectile(net.runelite.api.Projectile value)
    {
        this.value = Objects.requireNonNull(value, "value");
    }

    public int getId() { return value.getId(); }
    public int getID() { return getId(); }
    public int getEndCycle() { return value.getEndCycle(); }
    public int getStartCycle() { return value.getStartCycle(); }
    public int getZ() { return (int) Math.round(value.getZ()); }
    public int getFloorLevel() { return value.getFloor(); }
    public int getHeight() { return value.getHeight(); }
    public int getSlope() { return value.getSlope(); }
    public int getTargetIndex() { return value.getTargetActor() == null ? -1 : actorIndex(value.getTargetActor()); }
    public Actor getOriginActor() { return value.getSourceActor(); }
    public Actor getTargetActor() { return value.getTargetActor(); }
    public int getTargetX() { return value.getTargetPoint() == null ? -1 : value.getTargetPoint().getX(); }
    public int getTargetY() { return value.getTargetPoint() == null ? -1 : value.getTargetPoint().getY(); }
    public Tile getTargetTile() { return tile(value.getTargetPoint()); }
    public int getOriginX() { return value.getSourcePoint() == null ? -1 : value.getSourcePoint().getX(); }
    public int getOriginY() { return value.getSourcePoint() == null ? -1 : value.getSourcePoint().getY(); }
    public Tile getOriginTile() { return tile(value.getSourcePoint()); }
    public int getTargetDistance()
    {
        Tile origin = getOriginTile();
        Tile target = getTargetTile();
        return origin == null || target == null ? Integer.MAX_VALUE : (int) origin.distance(target);
    }
    public double getRawX() { return value.getX(); }
    public double getRawY() { return value.getY(); }
    public int getLocalX() { return (int) Math.round(value.getX()); }
    public int getLocalY() { return (int) Math.round(value.getY()); }
    public int getX() { Tile tile = getTile(); return tile == null ? -1 : tile.getX(); }
    public int getY() { Tile tile = getTile(); return tile == null ? -1 : tile.getY(); }
    public boolean isMoving() { return value.getRemainingCycles() > 0; }
    public boolean exists() { return value.getRemainingCycles() >= 0; }

    @Override
    public Tile getTile()
    {
        WorldPoint world = WorldPoint.fromLocal(DreamBotRebornApi.requireClient(),
            (int) Math.round(value.getX()), (int) Math.round(value.getY()), value.getFloor());
        return tile(world);
    }

    public net.runelite.api.Projectile unwrap() { return value; }

    private static Tile tile(WorldPoint point) { return point == null ? null : new Tile(point); }

    private static int actorIndex(Actor actor)
    {
        if (actor instanceof net.runelite.api.Player) return ((net.runelite.api.Player) actor).getId();
        if (actor instanceof net.runelite.api.NPC) return ((net.runelite.api.NPC) actor).getIndex();
        return -1;
    }

    @Override public String toString() { return "Projectile{" + getId() + ", " + getTile() + '}'; }
}
