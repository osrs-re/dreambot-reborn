package com.dreambotreborn.api.methods.interactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.graphics.GraphicsObject;

/** DreamBot-style snapshot queries for active scene graphics effects. */
public final class GraphicsObjects
{
    private static volatile List<GraphicsObject> snapshot = Collections.emptyList();

    private GraphicsObjects() { }

    public static List<GraphicsObject> all() { return snapshot; }

    public static List<GraphicsObject> all(Integer... ids)
    {
        return all(value -> matches(value.getId(), ids));
    }

    public static List<GraphicsObject> all(Filter<GraphicsObject> filter)
    {
        Objects.requireNonNull(filter, "filter");
        List<GraphicsObject> result = new ArrayList<>();
        for (GraphicsObject value : snapshot) if (filter.match(value)) result.add(value);
        return Collections.unmodifiableList(result);
    }

    public static GraphicsObject closest(Integer... ids) { return closest(value -> matches(value.getId(), ids)); }
    public static GraphicsObject closest(Filter<GraphicsObject> filter) { return closest(filter, localTile()); }

    public static GraphicsObject closest(Filter<GraphicsObject> filter, Tile origin)
    {
        GraphicsObject closest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (GraphicsObject value : snapshot)
        {
            if (!filter.match(value) || value.getTile() == null) continue;
            double candidate = origin == null ? 0 : value.getTile().distance(origin);
            if (closest == null || candidate < distance) { closest = value; distance = candidate; }
        }
        return closest;
    }

    public static void refresh()
    {
        net.runelite.api.Deque<net.runelite.api.GraphicsObject> values =
            DreamBotRebornApi.requireClient().getGraphicsObjects();
        List<GraphicsObject> result = new ArrayList<>();
        if (values != null) for (net.runelite.api.GraphicsObject value : values)
            if (value != null && !value.finished()) result.add(new GraphicsObject(value));
        snapshot = Collections.unmodifiableList(result);
    }

    public static void clear() { snapshot = Collections.emptyList(); }

    private static Tile localTile()
    {
        net.runelite.api.Player local = DreamBotRebornApi.requireClient().getLocalPlayer();
        return local == null ? null : new Tile(local.getWorldLocation());
    }

    private static boolean matches(int id, Integer... ids)
    {
        if (ids != null) for (Integer expected : ids) if (expected != null && expected == id) return true;
        return false;
    }
}
