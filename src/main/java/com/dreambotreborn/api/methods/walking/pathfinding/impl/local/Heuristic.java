package com.dreambotreborn.api.methods.walking.pathfinding.impl.local;

@FunctionalInterface
public interface Heuristic
{
    double calculate(int x, int y, int targetX, int targetY);
}
