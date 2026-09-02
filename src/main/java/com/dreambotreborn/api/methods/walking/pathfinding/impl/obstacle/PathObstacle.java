package com.dreambotreborn.api.methods.walking.pathfinding.impl.obstacle;

import java.util.Objects;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.utilities.impl.Condition;
import com.dreambotreborn.api.wrappers.interactive.GameObject;

public abstract class PathObstacle
{
    private String name;
    private String action;
    private Tile startTile;
    private Tile obstacleTile;
    private Tile endTile;
    private Condition condition;

    protected PathObstacle(String name, String action, Tile startTile, Tile obstacleTile, Tile endTile)
    {
        this.name = name; this.action = action; this.startTile = startTile;
        this.obstacleTile = obstacleTile; this.endTile = endTile;
    }
    public abstract PathObstacle duplicate(Tile tile);
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getAction() { return action; }
    public void setAction(String value) { action = value; }
    public Tile getEndTile() { return endTile; }
    public void setEndTile(Tile value) { endTile = value; }
    public Tile getObstacleTile() { return obstacleTile; }
    public void setObstacleTile(Tile value) { obstacleTile = value; }
    public Tile getStartTile() { return startTile; }
    public void setStartTile(Tile value) { startTile = value; }
    public GameObject getObstacle()
    {
        return GameObjects.closest(value -> (name == null || value.name.equalsIgnoreCase(name))
            && (obstacleTile == null || value.getTile().distance(obstacleTile) <= 1));
    }
    public boolean exists() { return getObstacle() != null; }
    public abstract boolean isCompleted();
    public boolean isValid() { return exists() && !isCompleted(); }
    public boolean traverse()
    {
        GameObject object = getObstacle();
        return object != null && (action == null ? object.interact() : object.interact(action));
    }
    public int getPriority() { return 0; }
    public Condition getCondition() { return condition; }
    public void setCondition(Condition value) { condition = value; }
    @Override public boolean equals(Object value)
    {
        if (!(value instanceof PathObstacle)) return false;
        PathObstacle other = (PathObstacle) value;
        return Objects.equals(name, other.name) && Objects.equals(obstacleTile, other.obstacleTile);
    }
    @Override public int hashCode() { return Objects.hash(name, obstacleTile); }
}
