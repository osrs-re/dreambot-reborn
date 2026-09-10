package com.dreambotreborn.api.methods.walking.pathfinding.impl.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.path.impl.LocalPath;
import com.dreambotreborn.api.methods.walking.pathfinding.data.TileFlags;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.node.PathNode;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.obstacle.PathObstacle;
import com.dreambotreborn.api.utilities.impl.Condition;

/** Collision-aware A* over one loaded RuneScape scene. */
public final class LocalPathFinder
{
    private static final LocalPathFinder INSTANCE = new LocalPathFinder();
    private static volatile List<PathNode> lastOpen = Collections.emptyList();
    private static volatile List<PathNode> lastClosed = Collections.emptyList();
    private static final int[][] DIRECTIONS =
    {
        {-1, -1}, {0, -1}, {1, -1},
        {-1, 0},             {1, 0},
        {-1, 1},  {0, 1},   {1, 1}
    };

    private final Heuristic heuristic;
    private final Set<Tile> blacklistedTiles = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<Tile> webOnlyTiles = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Map<Tile, Condition> tileConditions = Collections.synchronizedMap(new LinkedHashMap<>());
    private final List<PathObstacle> obstacles = new CopyOnWriteArrayList<>();
    private volatile int currentDepth;

    public LocalPathFinder()
    {
        this(new AbsoluteHeuristic());
    }

    public LocalPathFinder(Heuristic heuristic)
    {
        this.heuristic = heuristic == null ? new AbsoluteHeuristic() : heuristic;
    }

    public static LocalPathFinder getLocalPathFinder() { return INSTANCE; }
    public static List<PathNode> getOpen() { return lastOpen; }
    public static List<PathNode> getClosed() { return lastClosed; }
    public void addBlacklistedTile(Tile tile) { if (tile != null) blacklistedTiles.add(tile); }
    public void removeBlacklistedTile(Tile tile) { blacklistedTiles.remove(tile); }
    public boolean isBlacklisted(Tile tile) { return tile != null && blacklistedTiles.contains(tile); }
    public void clearBlacklist() { blacklistedTiles.clear(); }
    public Set<Tile> getBlacklistedTiles()
    {
        synchronized (blacklistedTiles)
        { return Collections.unmodifiableSet(new LinkedHashSet<>(blacklistedTiles)); }
    }
    public void addWebOnlyObstacleTile(Tile tile) { if (tile != null) webOnlyTiles.add(tile); }
    public void removeWebOnlyObstacleTile(Tile tile) { webOnlyTiles.remove(tile); }
    public boolean isTileWebOnly(Tile tile) { return tile != null && webOnlyTiles.contains(tile); }
    public void addTileCondition(Tile tile, Condition condition)
    { if (tile != null && condition != null) tileConditions.put(tile, condition); }
    public void removeTileCondition(Tile tile) { tileConditions.remove(tile); }
    public void clearTileConditions() { tileConditions.clear(); }
    public boolean checkTileCondition(Tile tile)
    {
        Condition condition = tileConditions.get(tile);
        return condition == null || condition.verify();
    }
    public void addObstacle(PathObstacle obstacle) { if (obstacle != null) obstacles.add(obstacle); }
    public void removeObstacle(PathObstacle obstacle) { obstacles.remove(obstacle); }
    public int getCurrentDepth() { return currentDepth; }
    public void setCurrentDepth(int value) { currentDepth = Math.max(0, value); }
    public float getHeuristicCost(int x, int y, int targetX, int targetY)
    { return (float) heuristic.calculate(x, y, targetX, targetY); }
    public float getHeuristicCost(int x, int y, int z, int targetX, int targetY, int targetZ)
    { return z == targetZ ? getHeuristicCost(x, y, targetX, targetY) : Float.POSITIVE_INFINITY; }
    public LocalPath<Tile> calculate(Tile start, Tile destination)
    {
        if (start == null || destination == null) return new LocalPath<>(Collections.emptyList());
        net.runelite.api.CollisionData[] maps =
            com.dreambotreborn.api.DreamBotRebornApi.requireClient().getCollisionMaps();
        int plane = start.getZ();
        if (maps == null || plane < 0 || plane >= maps.length || maps[plane] == null)
            return new LocalPath<>(Collections.emptyList());
        return new LocalPath<>(find(maps[plane].getFlags(),
            com.dreambotreborn.api.DreamBotRebornApi.requireClient().getBaseX(),
            com.dreambotreborn.api.DreamBotRebornApi.requireClient().getBaseY(), plane,
            start, destination));
    }
    public LocalPath<Tile> calculate(int startX, int startY, int endX, int endY)
    {
        int plane = com.dreambotreborn.api.DreamBotRebornApi.requireClient().getPlane();
        return calculate(startX, startY, plane, endX, endY, plane);
    }
    public LocalPath<Tile> calculate(int startX, int startY, int endX, int endY, int plane)
    { return calculate(startX, startY, plane, endX, endY, plane); }
    public LocalPath<Tile> calculate(int startX, int startY, int startZ,
                                     int endX, int endY, int endZ)
    {
        return calculate(new Tile(startX, startY, startZ), new Tile(endX, endY, endZ));
    }
    public double getWalkingDistance(Tile start, Tile destination)
    {
        LocalPath<Tile> path = calculate(start, destination);
        return path.isEmpty() ? Double.POSITIVE_INFINITY : Math.max(0, path.size() - 1);
    }
    public void onWebNodeVersionUpdate() { webOnlyTiles.clear(); }
    public void reset()
    {
        clearBlacklist(); clearTileConditions(); webOnlyTiles.clear(); obstacles.clear(); currentDepth = 0;
    }

    public List<Tile> find(
        int[][] flags, int baseX, int baseY, int plane, Tile start, Tile destination)
    {
        if (flags == null || flags.length == 0 || start == null || destination == null
            || start.getZ() != plane || destination.getZ() != plane)
        {
            return Collections.emptyList();
        }
        int startX = start.getX() - baseX;
        int startY = start.getY() - baseY;
        int targetX = destination.getX() - baseX;
        int targetY = destination.getY() - baseY;
        if (!inside(flags, startX, startY) || !inside(flags, targetX, targetY))
        {
            return Collections.emptyList();
        }

        double[][] costs = new double[flags.length][];
        boolean[][] closed = new boolean[flags.length][];
        for (int x = 0; x < flags.length; x++)
        {
            int height = flags[x] == null ? 0 : flags[x].length;
            costs[x] = new double[height];
            java.util.Arrays.fill(costs[x], Double.POSITIVE_INFINITY);
            closed[x] = new boolean[height];
        }
        PriorityQueue<PathNode> open = new PriorityQueue<>();
        List<PathNode> opened = new ArrayList<>();
        List<PathNode> closedNodes = new ArrayList<>();
        costs[startX][startY] = 0.0;
        PathNode startNode = new PathNode(startX, startY, 0.0,
            heuristic.calculate(startX, startY, targetX, targetY), null);
        open.add(startNode);
        opened.add(startNode);

        int expanded = 0;
        int maximum = Math.max(4096, flags.length * 128);
        while (!open.isEmpty() && expanded++ < maximum)
        {
            PathNode current = open.poll();
            if (closed[current.sceneX][current.sceneY]) continue;
            closed[current.sceneX][current.sceneY] = true;
            closedNodes.add(current);
            currentDepth = Math.max(currentDepth, (int) Math.ceil(current.cost));
            if (current.sceneX == targetX && current.sceneY == targetY)
            {
                lastOpen = Collections.unmodifiableList(new ArrayList<>(opened));
                lastClosed = Collections.unmodifiableList(new ArrayList<>(closedNodes));
                return build(current, baseX, baseY, plane);
            }
            for (int[] direction : DIRECTIONS)
            {
                int nextX = current.sceneX + direction[0];
                int nextY = current.sceneY + direction[1];
                Tile nextTile = new Tile(baseX + nextX, baseY + nextY, plane);
                if (!inside(flags, nextX, nextY) || closed[nextX][nextY]
                    || isBlacklisted(nextTile) || isTileWebOnly(nextTile)
                    || !checkTileCondition(nextTile)
                    || !canMove(flags, current.sceneX, current.sceneY, direction[0], direction[1]))
                {
                    continue;
                }
                double cost = current.cost + (direction[0] != 0 && direction[1] != 0
                    ? Math.sqrt(2.0) : 1.0);
                if (cost >= costs[nextX][nextY]) continue;
                costs[nextX][nextY] = cost;
                double score = cost + heuristic.calculate(nextX, nextY, targetX, targetY);
                PathNode next = new PathNode(nextX, nextY, cost, score, current);
                open.add(next);
                opened.add(next);
            }
        }
        lastOpen = Collections.unmodifiableList(new ArrayList<>(opened));
        lastClosed = Collections.unmodifiableList(new ArrayList<>(closedNodes));
        return Collections.emptyList();
    }

    public boolean canMove(int[][] flags, int x, int y, int dx, int dy)
    {
        int nextX = x + dx;
        int nextY = y + dy;
        if (!inside(flags, x, y) || !inside(flags, nextX, nextY)
            || blocked(flags[nextX][nextY])) return false;
        if (dx == 0 && dy == 1) return clear(flags[x][y], TileFlags.NORTH)
            && clear(flags[nextX][nextY], TileFlags.SOUTH);
        if (dx == 0 && dy == -1) return clear(flags[x][y], TileFlags.SOUTH)
            && clear(flags[nextX][nextY], TileFlags.NORTH);
        if (dx == 1 && dy == 0) return clear(flags[x][y], TileFlags.EAST)
            && clear(flags[nextX][nextY], TileFlags.WEST);
        if (dx == -1 && dy == 0) return clear(flags[x][y], TileFlags.WEST)
            && clear(flags[nextX][nextY], TileFlags.EAST);
        if (dx != 0 && dy != 0)
        {
            if (!canMove(flags, x, y, dx, 0) || !canMove(flags, x, y, 0, dy)) return false;
            int diagonalFrom = dx > 0
                ? (dy > 0 ? TileFlags.NORTH_EAST : TileFlags.SOUTH_EAST)
                : (dy > 0 ? TileFlags.NORTH_WEST : TileFlags.SOUTH_WEST);
            int diagonalTo = dx > 0
                ? (dy > 0 ? TileFlags.SOUTH_WEST : TileFlags.NORTH_WEST)
                : (dy > 0 ? TileFlags.SOUTH_EAST : TileFlags.NORTH_EAST);
            return clear(flags[x][y], diagonalFrom) && clear(flags[nextX][nextY], diagonalTo);
        }
        return false;
    }

    private static boolean inside(int[][] flags, int x, int y)
    {
        return x >= 0 && x < flags.length && flags[x] != null
            && y >= 0 && y < flags[x].length;
    }

    private static boolean blocked(int flags)
    {
        return (flags & TileFlags.BLOCKED) != 0;
    }

    private static boolean clear(int flags, int mask)
    {
        return (flags & mask) == 0;
    }

    private static List<Tile> build(PathNode node, int baseX, int baseY, int plane)
    {
        List<Tile> result = new ArrayList<>();
        for (PathNode current = node; current != null; current = current.parent)
            result.add(current.toTile(baseX, baseY, plane));
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }
}
