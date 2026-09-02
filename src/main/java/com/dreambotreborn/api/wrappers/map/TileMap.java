package com.dreambotreborn.api.wrappers.map;

public interface TileMap
{
    int getWidth();
    int getHeight();
    boolean isSolid(int x, int y);
    boolean isSolid(int x, int y, int mask);
    boolean isWalkable(int x, int y, int dx, int dy);
    boolean isWalkable(int x, int y, int dx, int dy, int size);
    float getCost(int x, int y, int dx, int dy);
    void visit(int x, int y);
    int getDirection(int x, int y);
    int getFlag(int x, int y);
    int getBaseX();
    int getBaseY();
}
