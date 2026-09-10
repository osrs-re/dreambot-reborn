package com.dreambotreborn.api.wrappers.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.interactive.BoundaryObject;
import com.dreambotreborn.api.wrappers.interactive.FloorDecoration;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.wrappers.interactive.SceneObject;
import com.dreambotreborn.api.wrappers.interactive.WallObject;
import com.dreambotreborn.api.wrappers.items.ItemLayer;

public class TileReference
{
    private final net.runelite.api.Tile reference;
    public TileReference(Object reference)
    {
        if (!(reference instanceof net.runelite.api.Tile))
            throw new IllegalArgumentException("reference must be a RuneLite Tile");
        this.reference = (net.runelite.api.Tile) reference;
    }
    public net.runelite.api.Tile getReference() { return reference; }
    public ItemLayer getItemLayer()
    {
        return reference.getItemLayer() == null ? null : new ItemLayer(reference.getItemLayer());
    }
    public FloorDecoration getFloorDecoration()
    {
        return reference.getGroundObject() == null ? null
            : new FloorDecoration(reference.getGroundObject());
    }
    public int getSceneObjectCount() { return getSceneObjects().size(); }
    public List<GameObject> getObjectsForId(int id)
    {
        List<GameObject> result = new ArrayList<>();
        for (GameObject object : getObjects()) if (object.getId() == id) result.add(object);
        return Collections.unmodifiableList(result);
    }
    public GameObject[] getObjects()
    {
        List<GameObject> result = new ArrayList<>();
        BoundaryObject boundary = getBoundaryObject();
        WallObject wall = getWallObject();
        FloorDecoration floor = getFloorDecoration();
        if (boundary != null) result.add(boundary);
        if (wall != null) result.add(wall);
        if (floor != null) result.add(floor);
        result.addAll(getSceneObjects());
        return result.toArray(new GameObject[0]);
    }
    public WallObject getWallObject()
    {
        return reference.getDecorativeObject() == null ? null
            : new WallObject(reference.getDecorativeObject());
    }
    public BoundaryObject getBoundaryObject()
    {
        return reference.getWallObject() == null ? null
            : new BoundaryObject(reference.getWallObject());
    }
    public List<SceneObject> getSceneObjects()
    {
        List<SceneObject> result = new ArrayList<>();
        net.runelite.api.GameObject[] objects = reference.getGameObjects();
        if (objects != null) for (net.runelite.api.GameObject object : objects)
            if (object != null) result.add(new SceneObject(object));
        return Collections.unmodifiableList(result);
    }
    public int getGridX() { return reference.getSceneLocation().getX(); }
    public int getGridY() { return reference.getSceneLocation().getY(); }
    public int getZ() { return reference.getPlane(); }
    public Tile getTile() { return new Tile(reference.getWorldLocation()); }
    public int getFlags()
    {
        net.runelite.api.CollisionData[] maps = DreamBotRebornApi.requireClient().getCollisionMaps();
        int plane = getZ();
        return maps == null || plane < 0 || plane >= maps.length || maps[plane] == null
            ? 0 : getFlags(maps[plane].getFlags());
    }
    public int getFlags(int[][] flags)
    {
        int x = getGridX(), y = getGridY();
        return flags == null || x < 0 || x >= flags.length || flags[x] == null
            || y < 0 || y >= flags[x].length ? 0 : flags[x][y];
    }
}
