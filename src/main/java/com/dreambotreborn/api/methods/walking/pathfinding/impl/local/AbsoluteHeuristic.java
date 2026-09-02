package com.dreambotreborn.api.methods.walking.pathfinding.impl.local;

/** Octile-distance heuristic suitable for eight-directional movement. */
public final class AbsoluteHeuristic implements Heuristic
{
    @Override
    public double calculate(int x, int y, int targetX, int targetY)
    {
        int dx = Math.abs(targetX - x);
        int dy = Math.abs(targetY - y);
        return Math.max(dx, dy) + (Math.sqrt(2.0) - 1.0) * Math.min(dx, dy);
    }
}
