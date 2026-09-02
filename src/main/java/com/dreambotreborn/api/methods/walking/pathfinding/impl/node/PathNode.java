package com.dreambotreborn.api.methods.walking.pathfinding.impl.node;

import com.dreambotreborn.api.methods.map.Tile;

/** A* node exposed for diagnostics and custom pathfinding. */
public final class PathNode implements Comparable<PathNode>
{
    public final int sceneX;
    public final int sceneY;
    public final double cost;
    public final double score;
    public final PathNode parent;

    public PathNode(int sceneX, int sceneY, double cost, double score, PathNode parent)
    {
        this.sceneX = sceneX;
        this.sceneY = sceneY;
        this.cost = cost;
        this.score = score;
        this.parent = parent;
    }

    public Tile toTile(int baseX, int baseY, int plane)
    {
        return new Tile(baseX + sceneX, baseY + sceneY, plane);
    }

    @Override
    public int compareTo(PathNode other)
    {
        return Double.compare(score, other.score);
    }
}
