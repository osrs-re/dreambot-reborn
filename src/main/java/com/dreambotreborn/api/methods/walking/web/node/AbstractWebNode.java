package com.dreambotreborn.api.methods.walking.web.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.wrappers.interactive.Locatable;

public abstract class AbstractWebNode implements Locatable
{
    private final Tile tile;
    private final List<AbstractWebNode> connections = new ArrayList<>();
    private int weight = 1;
    private int index = -1;
    protected AbstractWebNode(int x, int y, int z) { tile = new Tile(x, y, z); }
    public int getWeight() { return weight; }
    public void setWeight(int value) { weight = Math.max(0, value); }
    public abstract WebNodeType getType();
    public int getX() { return tile.getX(); }
    public int getY() { return tile.getY(); }
    public int getZ() { return tile.getZ(); }
    public int getGridX() { return tile.getGridX(); }
    public int getGridY() { return tile.getGridY(); }
    public boolean hasRequirements() { return false; }
    public double distance(Tile value) { return tile.distance(value); }
    @Override public Tile getTile() { return tile; }
    public void setConnections(List<AbstractWebNode> values)
    {
        connections.clear(); if (values != null) connections.addAll(values);
    }
    public void initializeConnections(List<AbstractWebNode> values) { setConnections(values); }
    public void addDualConnections(AbstractWebNode... values)
    {
        addOutgoingConnections(values);
        if (values != null) for (AbstractWebNode value : values)
            if (value != null) value.addOutgoingConnections(this);
    }
    public void addOutgoingConnections(AbstractWebNode... values)
    {
        if (values != null) for (AbstractWebNode value : values)
            if (value != null && value != this && !connections.contains(value)) connections.add(value);
    }
    public void addIncomingConnections(AbstractWebNode... values)
    {
        if (values != null) for (AbstractWebNode value : values)
            if (value != null) value.addOutgoingConnections(this);
    }
    public void addConnections(AbstractWebNode... values) { addOutgoingConnections(values); }
    public void removeConnections(AbstractWebNode... values)
    {
        if (values != null) for (AbstractWebNode value : values) connections.remove(value);
    }
    public void removeDualConnections(AbstractWebNode... values)
    {
        removeConnections(values);
        if (values != null) for (AbstractWebNode value : values)
            if (value != null) value.removeConnections(this);
    }
    public void clear() { connections.clear(); }
    public int getIndex() { return index; }
    public void setIndex(int value) { index = value; }
    public boolean execute() { return Walking.walk(tile); }
    public boolean forceNext() { return false; }
    public boolean isValid() { return true; }
    public double walkingDistance(Tile value) { return tile.walkingDistance(value); }
    public List<AbstractWebNode> getConnections() { return Collections.unmodifiableList(connections); }
    @Override public boolean equals(Object value)
    {
        return value instanceof AbstractWebNode && Objects.equals(tile, ((AbstractWebNode) value).tile);
    }
    @Override public int hashCode() { return tile.hashCode(); }
    @Override public String toString() { return getType() + " " + tile; }
}
