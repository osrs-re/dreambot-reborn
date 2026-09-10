package com.dreambotreborn.api.methods.map;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.input.mouse.destination.impl.MiniMapTileDestination;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.walking.TileObstacle;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import com.dreambotreborn.api.methods.walking.pathfinding.data.TileFlags;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.wrappers.map.TileReference;
import net.runelite.api.CollisionData;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;

/** Scene conversion, collision and reachability helpers. */
public final class Map
{
    private static final Set<String> OBSTACLE_ACTIONS =
        Collections.synchronizedSet(new LinkedHashSet<>());
    static
    {
        Collections.addAll(OBSTACLE_ACTIONS, "Open", "Climb", "Climb-up", "Climb-down",
            "Enter", "Exit", "Cross", "Jump-over", "Squeeze-through", "Pass");
    }
    private Map() { }
    public static boolean isVisible(Tile tile) { return isTileOnMap(tile) || isTileOnScreen(tile); }
    public static Tile getWalkable(Tile tile)
    {
        if (tile == null) return null;
        if (canReach(tile)) return tile;
        for (int radius = 1; radius <= 10; radius++)
            for (int x = -radius; x <= radius; x++) for (int y = -radius; y <= radius; y++)
            {
                if (Math.abs(x) != radius && Math.abs(y) != radius) continue;
                Tile candidate = tile.translate(x, y);
                if (canReach(candidate)) return candidate;
            }
        return null;
    }
    public static TileReference getTileReference(Tile tile)
    {
        return tile == null ? null : getTileReference(tile.getX(), tile.getY(), tile.getZ());
    }
    public static TileReference getTileReference(int x, int y, int plane)
    {
        net.runelite.api.WorldView view = DreamBotRebornApi.requireClient().getTopLevelWorldView();
        net.runelite.api.Scene scene = view == null ? null : view.getScene();
        net.runelite.api.Tile[][][] tiles = scene == null ? null : scene.getTiles();
        int gridX = x - DreamBotRebornApi.requireClient().getBaseX();
        int gridY = y - DreamBotRebornApi.requireClient().getBaseY();
        if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null
            || gridX < 0 || gridX >= tiles[plane].length || tiles[plane][gridX] == null
            || gridY < 0 || gridY >= tiles[plane][gridX].length) return null;
        net.runelite.api.Tile reference = tiles[plane][gridX][gridY];
        return reference == null ? null : new TileReference(reference);
    }
    public static TileReference[][] getTileGrid(int plane)
    {
        net.runelite.api.WorldView view = DreamBotRebornApi.requireClient().getTopLevelWorldView();
        net.runelite.api.Scene scene = view == null ? null : view.getScene();
        net.runelite.api.Tile[][][] tiles = scene == null ? null : scene.getTiles();
        if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null)
            return new TileReference[0][0];
        TileReference[][] result = new TileReference[tiles[plane].length][];
        for (int x = 0; x < result.length; x++)
        {
            net.runelite.api.Tile[] column = tiles[plane][x];
            result[x] = new TileReference[column == null ? 0 : column.length];
            for (int y = 0; y < result[x].length; y++)
                if (column[y] != null) result[x][y] = new TileReference(column[y]);
        }
        return result;
    }
    public static Shape getMinimapShape() { return getMinimapShape(0); }
    public static Shape getMinimapShape(int inset)
    {
        net.runelite.api.widgets.Widget widget = minimapWidget();
        Rectangle bounds = widget == null ? null : widget.getBounds();
        if (bounds == null) return new Rectangle();
        int safe = Math.max(0, inset);
        return new Ellipse2D.Double(bounds.x + safe, bounds.y + safe,
            Math.max(0, bounds.width - safe * 2), Math.max(0, bounds.height - safe * 2));
    }
    public static boolean hoveringMinimap() { return getMinimapShape().contains(Mouse.getPosition()); }
    public static int getTileHeight(Tile tile)
    {
        LocalPoint local = local(tile);
        return local == null ? 0 : Perspective.getTileHeight(
            DreamBotRebornApi.requireClient(), local, tile.getZ());
    }
    public static int getGridX(Tile tile) { return tile == null ? -1 : tile.getGridX(); }
    public static int getGridY(Tile tile) { return tile == null ? -1 : tile.getGridY(); }
    public static Tile getGridLocation(Tile tile)
    {
        return tile == null ? null : new Tile(getGridX(tile), getGridY(tile), tile.getZ());
    }
    public static Point[] getVertices(Tile tile)
    {
        Polygon polygon = getPolygon(tile);
        if (polygon == null) return new Point[0];
        Point[] result = new Point[polygon.npoints];
        for (int i = 0; i < result.length; i++) result[i] = new Point(polygon.xpoints[i], polygon.ypoints[i]);
        return result;
    }
    public static Rectangle getBounds(Tile tile)
    {
        Polygon polygon = getPolygon(tile); return polygon == null ? new Rectangle() : polygon.getBounds();
    }
    public static Polygon getPolygon(Tile tile)
    {
        LocalPoint local = local(tile);
        return local == null ? null : Perspective.getCanvasTilePoly(DreamBotRebornApi.requireClient(), local);
    }
    public static boolean isLocal(Tile tile) { return local(tile) != null; }
    public static GameObject[] getObjectsAtTile(Tile tile) { return getObjectsAtTile(tile, true); }
    public static GameObject[] getObjectsAtTile(Tile tile, boolean includeDecorations)
    {
        GameObject[] objects = GameObjects.getObjectsOnTile(tile);
        if (includeDecorations) return objects;
        List<GameObject> result = new ArrayList<>();
        for (GameObject object : objects)
            if (object.getObjectType() == GameObject.Type.GAME) result.add(object);
        return result.toArray(new GameObject[0]);
    }
    public static MiniMapTileDestination getMiniMapDestination(Tile tile)
    {
        return tile == null ? null : new MiniMapTileDestination(tile);
    }
    public static Point tileToMiniMap(Tile tile)
    {
        LocalPoint local = local(tile);
        net.runelite.api.Point point = local == null ? null
            : Perspective.localToMinimap(DreamBotRebornApi.requireClient(), local);
        return point == null ? null : new Point(point.getX(), point.getY());
    }
    public static Point tileToScreen(Tile tile)
    {
        LocalPoint local = local(tile);
        net.runelite.api.Point point = local == null ? null
            : Perspective.localToCanvas(DreamBotRebornApi.requireClient(), local, tile.getZ());
        return point == null ? null : new Point(point.getX(), point.getY());
    }
    public static boolean isTileOnMap(Tile tile)
    {
        Point point = tileToMiniMap(tile); return point != null && getMinimapShape().contains(point);
    }
    public static boolean isTileOnScreen(Tile tile)
    {
        Point point = tileToScreen(tile);
        return point != null && point.x >= 0 && point.y >= 0
            && point.x < DreamBotRebornApi.requireClient().getCanvasWidth()
            && point.y < DreamBotRebornApi.requireClient().getCanvasHeight();
    }
    public static boolean isClickable(Tile tile)
    {
        Polygon polygon = getPolygon(tile); return polygon != null && polygon.getBounds().width > 0;
    }
    public static int getFlag(Tile tile)
    {
        TileReference reference = getTileReference(tile); return reference == null ? 0 : reference.getFlags();
    }
    public static boolean interact(Tile tile) { return Walking.walkOnScreen(tile); }
    public static boolean interact(Tile tile, String action) { return interact(tile); }
    public static boolean canReach(Entity entity) { return entity != null && canReach(entity.getTile()); }
    public static boolean canReach(Tile tile) { return canReach(localPlayerTile(), tile, false); }
    public static boolean canReach(Tile from, Tile tile) { return canReach(from, tile, false); }
    public static boolean canReach(Tile tile, boolean allowObjectCollision)
    { return canReach(localPlayerTile(), tile, allowObjectCollision); }
    public static boolean canReach(Tile from, Tile tile, boolean allowObjectCollision)
    { return exactDistance(from, tile, allowObjectCollision) >= 0; }
    public static int exactDistance(Entity entity) { return entity == null ? -1 : exactDistance(entity.getTile()); }
    public static int exactDistance(Tile tile) { return exactDistance(localPlayerTile(), tile, false); }
    public static int exactDistance(Tile from, Tile tile) { return exactDistance(from, tile, false); }
    public static int exactDistance(Tile tile, boolean allowObjectCollision)
    { return exactDistance(localPlayerTile(), tile, allowObjectCollision); }
    public static int exactDistance(Tile from, Tile tile, boolean allowObjectCollision)
    {
        if (from == null || tile == null || from.getZ() != tile.getZ()) return -1;
        CollisionData[] maps = DreamBotRebornApi.requireClient().getCollisionMaps();
        int plane = from.getZ();
        if (maps == null || plane < 0 || plane >= maps.length || maps[plane] == null) return -1;
        int[][] flags = maps[plane].getFlags();
        if (allowObjectCollision) flags = withoutObjectCollision(flags);
        List<Tile> path = LocalPathFinder.getLocalPathFinder().find(flags,
            DreamBotRebornApi.requireClient().getBaseX(), DreamBotRebornApi.requireClient().getBaseY(),
            plane, from, tile);
        return path.isEmpty() ? -1 : Math.max(0, path.size() - 1);
    }
    public static HashMap<Tile, TileObstacle> getObstacleLocations()
    {
        HashMap<Tile, TileObstacle> result = new HashMap<>();
        for (GameObject object : GameObjects.all())
            for (String action : object.actions) if (OBSTACLE_ACTIONS.contains(action))
            {
                TileObstacle.Type type = action.toLowerCase().contains("up")
                    ? TileObstacle.Type.ASCENDING : action.toLowerCase().contains("down")
                    ? TileObstacle.Type.DESCENDING : TileObstacle.Type.ENTRY;
                result.put(object.getTile(), new TileObstacle(type, object));
                break;
            }
        return result;
    }
    public static List<String> getObstacleActions()
    {
        synchronized (OBSTACLE_ACTIONS) { return new ArrayList<>(OBSTACLE_ACTIONS); }
    }
    public static void addObstacleActions(String... actions)
    {
        if (actions != null) for (String action : actions) if (action != null) OBSTACLE_ACTIONS.add(action);
    }
    public static void removeObstacleActions(String... actions)
    {
        if (actions != null) for (String action : actions) OBSTACLE_ACTIONS.remove(action);
    }
    public static int costToTile(int startX, int startY, int endX, int endY, boolean allowObjectCollision)
    {
        return costToTile(startX, startY, endX, endY,
            DreamBotRebornApi.requireClient().getPlane(), allowObjectCollision);
    }
    public static int costToTile(int startX, int startY, int endX, int endY, int plane,
                                 boolean allowObjectCollision)
    {
        return exactDistance(new Tile(startX, startY, plane), new Tile(endX, endY, plane),
            allowObjectCollision);
    }
    public static void clearGameObjectCache() { GameObjects.refresh(); }
    public static void reset() { }

    private static Tile localPlayerTile()
    {
        net.runelite.api.Player player = DreamBotRebornApi.requireClient().getLocalPlayer();
        return player == null ? null : new Tile(player.getWorldLocation());
    }
    private static LocalPoint local(Tile tile)
    {
        return tile == null ? null
            : LocalPoint.fromWorld(DreamBotRebornApi.requireClient(), tile.toWorldPoint());
    }
    private static net.runelite.api.widgets.Widget minimapWidget()
    {
        net.runelite.api.Client client = DreamBotRebornApi.requireClient();
        net.runelite.api.widgets.Widget widget = client.getWidget(
            net.runelite.api.widgets.WidgetInfo.RESIZABLE_MINIMAP_DRAW_AREA);
        if (widget == null || widget.isHidden())
            widget = client.getWidget(net.runelite.api.widgets.WidgetInfo.FIXED_VIEWPORT_MINIMAP_DRAW_AREA);
        return widget;
    }
    private static int[][] withoutObjectCollision(int[][] source)
    {
        int[][] result = new int[source.length][];
        for (int x = 0; x < source.length; x++)
        {
            if (source[x] == null) { result[x] = new int[0]; continue; }
            result[x] = source[x].clone();
            for (int y = 0; y < result[x].length; y++)
                result[x][y] &= ~(TileFlags.OBJECT_TILE | TileFlags.DECORATION_BLOCK);
        }
        return result;
    }
}
