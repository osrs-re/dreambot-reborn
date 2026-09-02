package com.dreambotreborn.api.methods.walking.pathfinding.data;

import net.runelite.api.CollisionDataFlag;

/** Collision masks used by the local pathfinder. */
public final class TileFlags
{
    public static final int BLOCKED = CollisionDataFlag.BLOCK_MOVEMENT_FULL
        | CollisionDataFlag.BLOCK_MOVEMENT_OBJECT
        | CollisionDataFlag.BLOCK_MOVEMENT_FLOOR
        | CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION;
    public static final int NORTH = CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
    public static final int NORTH_EAST = CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST;
    public static final int EAST = CollisionDataFlag.BLOCK_MOVEMENT_EAST;
    public static final int SOUTH_EAST = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST;
    public static final int SOUTH = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
    public static final int SOUTH_WEST = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST;
    public static final int WEST = CollisionDataFlag.BLOCK_MOVEMENT_WEST;
    public static final int NORTH_WEST = CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST;

    private TileFlags()
    {
    }
}
