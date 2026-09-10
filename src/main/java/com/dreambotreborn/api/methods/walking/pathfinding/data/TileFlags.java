package com.dreambotreborn.api.methods.walking.pathfinding.data;

import net.runelite.api.CollisionDataFlag;

/** Collision masks used by the local pathfinder. */
public final class TileFlags
{
    public static final int NULL = 0;
    public static final int WALL_BLOCK_NORTHWEST = CollisionDataFlag.BLOCK_MOVEMENT_NORTH_WEST;
    public static final int WALL_BLOCK_NORTH = CollisionDataFlag.BLOCK_MOVEMENT_NORTH;
    public static final int WALL_BLOCK_NORTHEAST = CollisionDataFlag.BLOCK_MOVEMENT_NORTH_EAST;
    public static final int WALL_BLOCK_EAST = CollisionDataFlag.BLOCK_MOVEMENT_EAST;
    public static final int WALL_BLOCK_SOUTHEAST = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_EAST;
    public static final int WALL_BLOCK_SOUTH = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
    public static final int WALL_BLOCK_SOUTHWEST = CollisionDataFlag.BLOCK_MOVEMENT_SOUTH_WEST;
    public static final int WALL_BLOCK_WEST = CollisionDataFlag.BLOCK_MOVEMENT_WEST;
    public static final int WALL_BLOCK_SIGHT_NORTHWEST = 512;
    public static final int WALL_BLOCK_SIGHT_NORTH = CollisionDataFlag.BLOCK_LINE_OF_SIGHT_NORTH;
    public static final int WALL_BLOCK_SIGHT_NORTHEAST = 2048;
    public static final int WALL_BLOCK_SIGHT_EAST = CollisionDataFlag.BLOCK_LINE_OF_SIGHT_EAST;
    public static final int WALL_BLOCK_SIGHT_SOUTHEAST = 8192;
    public static final int WALL_BLOCK_SIGHT_SOUTH = CollisionDataFlag.BLOCK_LINE_OF_SIGHT_SOUTH;
    public static final int WALL_BLOCK_SIGHT_SOUTHWEST = 32768;
    public static final int WALL_BLOCK_SIGHT_WEST = CollisionDataFlag.BLOCK_LINE_OF_SIGHT_WEST;
    public static final int OBJECT_TILE = CollisionDataFlag.BLOCK_MOVEMENT_OBJECT;
    public static final int DECORATION_BLOCK = CollisionDataFlag.BLOCK_MOVEMENT_FLOOR_DECORATION;
    public static final int BLOCK_FLOOR = CollisionDataFlag.BLOCK_MOVEMENT_FLOOR;
    public static final int NON_WALKABLE = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
    public static final int WALL_ALLOW_RANGE_NORTHWEST = WALL_BLOCK_SIGHT_NORTHWEST;
    public static final int WALL_ALLOW_RANGE_NORTH = WALL_BLOCK_SIGHT_NORTH;
    public static final int WALL_ALLOW_RANGE_NORTHEAST = WALL_BLOCK_SIGHT_NORTHEAST;
    public static final int WALL_ALLOW_RANGE_EAST = WALL_BLOCK_SIGHT_EAST;
    public static final int WALL_ALLOW_RANGE_SOUTHEAST = WALL_BLOCK_SIGHT_SOUTHEAST;
    public static final int WALL_ALLOW_RANGE_SOUTH = WALL_BLOCK_SIGHT_SOUTH;
    public static final int WALL_ALLOW_RANGE_SOUTHWEST = WALL_BLOCK_SIGHT_SOUTHWEST;
    public static final int WALL_ALLOW_RANGE_WEST = WALL_BLOCK_SIGHT_WEST;
    public static final int OBJECT_ALLOW_RANGE = CollisionDataFlag.BLOCK_LINE_OF_SIGHT_FULL;
    public static final int UNLOADED = 0x1000000;
    public static final int OBJECT_BLOCK_SIGHT = CollisionDataFlag.BLOCK_LINE_OF_SIGHT_FULL;
    public static final int WALL_NORTHWEST = WALL_BLOCK_NORTHWEST | WALL_BLOCK_SIGHT_NORTHWEST;
    public static final int WALL_NORTH = WALL_BLOCK_NORTH | WALL_BLOCK_SIGHT_NORTH;
    public static final int WALL_NORTHEAST = WALL_BLOCK_NORTHEAST | WALL_BLOCK_SIGHT_NORTHEAST;
    public static final int WALL_EAST = WALL_BLOCK_EAST | WALL_BLOCK_SIGHT_EAST;
    public static final int WALL_SOUTHEAST = WALL_BLOCK_SOUTHEAST | WALL_BLOCK_SIGHT_SOUTHEAST;
    public static final int WALL_SOUTH = WALL_BLOCK_SOUTH | WALL_BLOCK_SIGHT_SOUTH;
    public static final int WALL_SOUTHWEST = WALL_BLOCK_SOUTHWEST | WALL_BLOCK_SIGHT_SOUTHWEST;
    public static final int WALL_WEST = WALL_BLOCK_WEST | WALL_BLOCK_SIGHT_WEST;
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
