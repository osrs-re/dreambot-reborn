package com.dreambotreborn.api.methods.walking.pathfinding.impl.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.pathfinding.data.TileFlags;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.node.PathNode;

/** Collision-aware A* over one loaded RuneScape scene. */
public final class LocalPathFinder
{
    private static final int[][] DIRECTIONS =
    {
        {-1, -1}, {0, -1}, {1, -1},
        {-1, 0},             {1, 0},
        {-1, 1},  {0, 1},   {1, 1}
    };

    private final Heuristic heuristic;

    public LocalPathFinder()
    {
        this(new AbsoluteHeuristic());
    }

    public LocalPathFinder(Heuristic heuristic)
    {
        this.heuristic = heuristic == null ? new AbsoluteHeuristic() : heuristic;
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
        costs[startX][startY] = 0.0;
        open.add(new PathNode(startX, startY, 0.0,
            heuristic.calculate(startX, startY, targetX, targetY), null));

        int expanded = 0;
        int maximum = Math.max(4096, flags.length * 128);
        while (!open.isEmpty() && expanded++ < maximum)
        {
            PathNode current = open.poll();
            if (closed[current.sceneX][current.sceneY]) continue;
            closed[current.sceneX][current.sceneY] = true;
            if (current.sceneX == targetX && current.sceneY == targetY)
            {
                return build(current, baseX, baseY, plane);
            }
            for (int[] direction : DIRECTIONS)
            {
                int nextX = current.sceneX + direction[0];
                int nextY = current.sceneY + direction[1];
                if (!inside(flags, nextX, nextY) || closed[nextX][nextY]
                    || !canMove(flags, current.sceneX, current.sceneY, direction[0], direction[1]))
                {
                    continue;
                }
                double cost = current.cost + (direction[0] != 0 && direction[1] != 0
                    ? Math.sqrt(2.0) : 1.0);
                if (cost >= costs[nextX][nextY]) continue;
                costs[nextX][nextY] = cost;
                double score = cost + heuristic.calculate(nextX, nextY, targetX, targetY);
                open.add(new PathNode(nextX, nextY, cost, score, current));
            }
        }
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
