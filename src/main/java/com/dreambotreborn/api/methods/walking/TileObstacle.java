package com.dreambotreborn.api.methods.walking;

import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.interactive.GameObject;

public class TileObstacle
{
    public enum Type { ASCENDING, DESCENDING, ENTRY, CUSTOM }
    private Type obstacleType;
    private GameObject obstacle;
    private Tile tile;
    private boolean completed;
    public TileObstacle(Type type, GameObject obstacle)
    {
        obstacleType = type == null ? Type.CUSTOM : type;
        setObstacle(obstacle);
    }
    public Tile getTile() { return tile; }
    public void setTile(Tile value) { tile = value; }
    public GameObject getObstacle() { return obstacle; }
    public void setObstacle(GameObject value)
    {
        obstacle = value;
        if (value != null) tile = value.getTile();
    }
    public Type getObstacleType() { return obstacleType; }
    public void setObstacleType(Type value) { obstacleType = value == null ? Type.CUSTOM : value; }
    public boolean isCompleted() { return completed || obstacle == null || !obstacle.exists(); }
    public void setCompleted(boolean value) { completed = value; }
    public void setEntry() { obstacleType = Type.ENTRY; }
    public void setAscending() { obstacleType = Type.ASCENDING; }
    public void setDescending() { obstacleType = Type.DESCENDING; }
    public boolean isEntry() { return obstacleType == Type.ENTRY; }
    public boolean isAscending() { return obstacleType == Type.ASCENDING; }
    public boolean isDescending() { return obstacleType == Type.DESCENDING; }
    public boolean handle()
    {
        String action = getAction();
        return obstacle != null && (action == null ? obstacle.interact() : obstacle.interact(action));
    }
    public String getAction()
    {
        if (obstacle == null) return null;
        String preferred = isAscending() ? "Climb-up" : isDescending() ? "Climb-down"
            : isEntry() ? "Enter" : null;
        if (preferred != null && obstacle.hasAction(preferred)) return preferred;
        return obstacle.getLeftClickAction();
    }
}
