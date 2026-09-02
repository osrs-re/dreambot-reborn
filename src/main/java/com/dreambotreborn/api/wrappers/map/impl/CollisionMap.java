package com.dreambotreborn.api.wrappers.map.impl;

import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.pathfinding.data.TileFlags;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import com.dreambotreborn.api.wrappers.map.TileMap;

public class CollisionMap implements TileMap
{
    private final net.runelite.api.CollisionData map;
    private final boolean[][] visited;
    public CollisionMap(net.runelite.api.CollisionData map)
    {
        this.map = map;
        int[][] flags = getFlags();
        visited = new boolean[flags.length][flags.length == 0 ? 0 : flags[0].length];
    }
    public static boolean isBlocked(int flag) { return (flag & TileFlags.BLOCKED) != 0; }
    public static boolean isCardinalDirectionBlocked(int current, int destination, int direction)
    {
        return isBlocked(destination) || isBlocked(current & direction);
    }
    public static boolean isOrdinalDirectionBlocked(
        int current, int horizontal, int vertical, int diagonal, int direction)
    {
        return isBlocked(current) || isBlocked(horizontal) || isBlocked(vertical) || isBlocked(diagonal);
    }
    public int getBaseX() { return DreamBotRebornApi.requireClient().getBaseX(); }
    public int getBaseY() { return DreamBotRebornApi.requireClient().getBaseY(); }
    public int getHeight() { int[][] flags = getFlags(); return flags.length == 0 ? 0 : flags[0].length; }
    public int getOffsetX() { return getBaseX(); }
    public int getOffsetY() { return getBaseY(); }
    public Tile getOffset() { return new Tile(getBaseX(), getBaseY(), DreamBotRebornApi.requireClient().getPlane()); }
    public int getWidth() { return getFlags().length; }
    public int[][] getFlags() { return map == null || map.getFlags() == null ? new int[0][0] : map.getFlags(); }
    public int getFlag(int x, int y)
    {
        int[][] flags = getFlags();
        return x < 0 || y < 0 || x >= flags.length || y >= flags[x].length
            ? TileFlags.BLOCKED : flags[x][y];
    }
    public boolean isBlocked(int x, int y, int mask) { return (getFlag(x, y) & mask) != 0; }
    public boolean isSolid(int x, int y) { return isBlocked(getFlag(x, y)); }
    public boolean isSolid(int x, int y, int mask) { return isBlocked(x, y, mask); }
    public float getCost(int x, int y, int dx, int dy)
    {
        if (!isWalkable(x, y, dx, dy)) return Float.POSITIVE_INFINITY;
        return dx != 0 && dy != 0 ? 1.41421356f : 1f;
    }
    public void visit(int x, int y)
    {
        if (x >= 0 && y >= 0 && x < visited.length && y < visited[x].length) visited[x][y] = true;
    }
    public boolean isWalkable(int x, int y, int dx, int dy)
    {
        return new LocalPathFinder().canMove(getFlags(), x, y, dx, dy);
    }
    public boolean isWalkable(int x, int y, int dx, int dy, int size)
    {
        if (size <= 1) return isWalkable(x, y, dx, dy);
        for (int offsetX = 0; offsetX < size; offsetX++)
            for (int offsetY = 0; offsetY < size; offsetY++)
                if (!isWalkable(x + offsetX, y + offsetY, dx, dy)) return false;
        return true;
    }
    public int getDirection(int x, int y)
    {
        return x < 0 || y < 0 || x >= visited.length || y >= visited[x].length || !visited[x][y] ? -1 : 0;
    }
}
