package com.dreambotreborn.api.methods.walking.path;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.obstacle.PathObstacle;
import com.dreambotreborn.api.wrappers.interactive.Locatable;

/** Mutable DreamBot-compatible ordered path. */
public abstract class AbstractPath<E extends Locatable> extends AbstractList<E>
{
    private final List<E> path = new ArrayList<>();
    private final Map<E, PathObstacle> obstacles = new LinkedHashMap<>();
    private PathDirection direction = PathDirection.FORWARD;

    protected AbstractPath() { }
    protected AbstractPath(Collection<? extends E> values)
    {
        if (values != null) path.addAll(values);
    }
    @Override public E get(int index) { return path.get(index); }
    @Override public int size() { return path.size(); }
    @Override public E set(int index, E element) { return path.set(index, element); }
    @Override public void add(int index, E element) { path.add(index, element); }
    @Override public E remove(int index) { return path.remove(index); }
    public E first() { return isEmpty() ? null : get(0); }
    public E last() { return isEmpty() ? null : get(size() - 1); }
    public void addToFront(E value) { add(0, value); }
    public List<E> path() { return Collections.unmodifiableList(path); }
    public List<Tile> getTiles()
    {
        List<Tile> result = new ArrayList<>(path.size());
        for (E value : path) if (value != null && value.getTile() != null) result.add(value.getTile());
        return Collections.unmodifiableList(result);
    }
    public Tile getStart() { E value = first(); return value == null ? null : value.getTile(); }
    public Tile getDestination() { E value = last(); return value == null ? null : value.getTile(); }
    public PathDirection direction() { return direction; }
    public void setDirection(PathDirection value)
    {
        direction = value == null ? PathDirection.LOST : value;
    }
    public Map<E, PathObstacle> obstacles() { return obstacles; }
    public boolean isObstacleTile(E value) { return obstacles.containsKey(value); }
    public PathObstacle getObstacleForTile(E value) { return obstacles.get(value); }
    public void reverse()
    {
        Collections.reverse(path);
        direction = direction == PathDirection.FORWARD
            ? PathDirection.BACKWARD : direction == PathDirection.BACKWARD
                ? PathDirection.FORWARD : PathDirection.LOST;
    }
    public abstract boolean walk();
    public abstract E next();
    public java.util.concurrent.CompletableFuture<Boolean> walkAsync()
    {
        return Walking.walkPathAsync(this);
    }
}
