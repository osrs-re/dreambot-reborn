package com.dreambotreborn.api.methods.walking.impl;

import java.awt.Rectangle;
import java.util.concurrent.CompletableFuture;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.input.mouse.destination.impl.MiniMapTileDestination;
import com.dreambotreborn.api.input.mouse.destination.impl.TileDestination;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.path.AbstractPath;
import com.dreambotreborn.api.methods.walking.path.impl.LocalPath;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.PathFinder;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.web.WebFinder;
import com.dreambotreborn.api.utilities.Await;

/** Local/minimap walking helpers; this is intentionally not a global pathfinder. */
public final class Walking
{
    private static volatile int runThreshold = 35;

    private Walking()
    {
    }

    public static boolean walk(int x, int y) { return Await.success(walkAsync(x, y)); }
    public static boolean walk(int x, int y, int plane)
    {
        return Await.success(walkAsync(x, y, plane));
    }
    public static boolean walk(WorldPoint tile) { return Await.success(walkAsync(tile)); }
    public static boolean walk(Tile tile) { return Await.success(walkAsync(tile)); }
    public static boolean walk(Entity entity) { return Await.success(walkAsync(entity)); }
    public static boolean clickTileOnMinimap(WorldPoint tile)
    {
        return Await.success(clickTileOnMinimapAsync(tile));
    }
    public static boolean clickTileOnMinimap(Tile tile)
    {
        return Await.success(clickTileOnMinimapAsync(tile));
    }
    public static boolean walkPath(AbstractPath<?> path) { return Await.success(walkPathAsync(path)); }
    public static boolean walkOnScreen(Tile tile) { return Await.success(walkOnScreenAsync(tile)); }
    public static boolean walkExact(Tile tile) { return Await.success(walkExactAsync(tile)); }

    public static CompletableFuture<Boolean> walkAsync(int x, int y)
    {
        return walkAsync(new WorldPoint(x, y, DreamBotRebornApi.requireClient().getPlane()));
    }

    public static CompletableFuture<Boolean> walkAsync(int x, int y, int plane)
    {
        return walkAsync(new WorldPoint(x, y, plane));
    }

    public static CompletableFuture<Boolean> walkAsync(WorldPoint tile)
    {
        if (tile == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        return walkAsync(new Tile(tile));
    }

    public static CompletableFuture<Boolean> walkAsync(Tile tile)
    {
        if (tile == null) return CompletableFuture.completedFuture(false);
        return PathFinder.findPath(tile).thenCompose(path ->
        {
            if (!path.isEmpty()) return walkPathAsync(path);
            net.runelite.api.Player local = DreamBotRebornApi.requireClient().getLocalPlayer();
            Tile start = local == null ? null : new Tile(local.getWorldLocation());
            if (start == null || start.distance(tile) <= 15)
                return clickTileOnMinimapDirect(tile);
            return walkPathAsync(WebFinder.getWebFinder().calculate(start, tile));
        });
    }

    public static CompletableFuture<Boolean> walkAsync(Entity entity)
    {
        return entity == null ? CompletableFuture.completedFuture(false)
            : walkAsync(entity.worldLocation);
    }

    public static CompletableFuture<Boolean> clickTileOnMinimapAsync(WorldPoint tile)
    {
        return walkAsync(tile);
    }

    public static CompletableFuture<Boolean> clickTileOnMinimapAsync(Tile tile)
    {
        return clickTileOnMinimapDirect(tile);
    }

    public static CompletableFuture<Boolean> walkPathAsync(AbstractPath<?> path)
    {
        if (path == null || path.isEmpty()) return CompletableFuture.completedFuture(false);
        return ClientThread.invoke(() -> furthestVisibleOnMinimap(path)).thenCompose(next ->
            next == null ? CompletableFuture.completedFuture(false)
                : Mouse.clickAsync(new MiniMapTileDestination(next)));
    }

    public static CompletableFuture<Boolean> walkOnScreenAsync(Tile tile)
    {
        return tile == null ? CompletableFuture.completedFuture(false)
            : Mouse.clickAsync(new TileDestination(tile));
    }

    public static CompletableFuture<Boolean> walkExactAsync(Tile tile)
    {
        return walkAsync(tile);
    }

    public static boolean shouldWalk()
    {
        return shouldWalk(4);
    }

    public static boolean shouldWalk(int distance)
    {
        int current = getDestinationDistance();
        return current < 0 || current <= Math.max(0, distance);
    }

    public static int getRunEnergy()
    {
        int energy = DreamBotRebornApi.requireClient().getEnergy();
        return energy > 100 ? energy / 100 : energy;
    }

    public static boolean isRunEnabled()
    {
        // The game's run-mode varp is 173 across the supported revision family.
        return DreamBotRebornApi.requireClient().getVarpValue(173) == 1;
    }

    public static boolean toggleRun()
    {
        return Await.success(toggleRunAsync());
    }

    public static CompletableFuture<Boolean> toggleRunAsync()
    {
        Widget widget = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.MINIMAP_TOGGLE_RUN_ORB);
        Rectangle bounds = widget == null ? null : widget.getBounds();
        if (bounds == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        int x = bounds.x + bounds.width / 2;
        int y = bounds.y + bounds.height / 2;
        return VirtualMouse.moveTo(x, y).thenApply(moved ->
        {
            if (moved)
            {
                VirtualMouse.click();
            }
            return moved;
        });
    }

    public static boolean isStaminaActive()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(
            net.runelite.api.Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 1;
    }

    public static int getRunThreshold()
    {
        return runThreshold;
    }

    public static void setRunThreshold(int threshold)
    {
        if (threshold < 0 || threshold > 100)
        {
            throw new IllegalArgumentException("threshold must be between 0 and 100");
        }
        runThreshold = threshold;
    }

    public static WorldPoint getDestination()
    {
        LocalPoint destination = DreamBotRebornApi.requireClient().getLocalDestinationLocation();
        return destination == null ? null : WorldPoint.fromLocal(DreamBotRebornApi.requireClient(), destination);
    }

    public static int getDestinationDistance()
    {
        WorldPoint destination = getDestination();
        Player player = Players.getLocal();
        if (destination == null || player == null || player.worldLocation == null)
        {
            return -1;
        }
        return player.worldLocation.distanceTo2D(destination);
    }

    public static boolean canWalk(WorldPoint tile)
    {
        return tile != null && LocalPoint.fromWorld(DreamBotRebornApi.requireClient(), tile) != null;
    }

    public static boolean canWalk(Tile tile)
    {
        return tile != null && canWalk(tile.toWorldPoint());
    }

    public static boolean canReach(Tile tile)
    {
        return Await.success(canReachAsync(tile));
    }

    public static CompletableFuture<Boolean> canReachAsync(Tile tile)
    {
        return PathFinder.findPath(tile).thenApply(path -> !path.isEmpty());
    }

    public static boolean canWalk(Entity entity)
    {
        return entity != null && canWalk(entity.worldLocation);
    }

    private static CompletableFuture<Boolean> clickTileOnMinimapDirect(Tile tile)
    {
        if (tile == null) return CompletableFuture.completedFuture(false);
        return ClientThread.invoke(() ->
        {
            Client client = DreamBotRebornApi.requireClient();
            LocalPoint local = LocalPoint.fromWorld(client, tile.toWorldPoint());
            return local == null || Perspective.localToMinimap(client, local) == null ? null : tile;
        }).thenCompose(visible -> visible == null ? CompletableFuture.completedFuture(false)
            : Mouse.clickAsync(new MiniMapTileDestination(visible)));
    }

    private static Tile furthestVisibleOnMinimap(AbstractPath<?> path)
    {
        Client client = DreamBotRebornApi.requireClient();
        java.util.List<Tile> tiles = path.getTiles();
        for (int index = tiles.size() - 1; index >= 0; index--)
        {
            Tile tile = tiles.get(index);
            LocalPoint local = LocalPoint.fromWorld(client, tile.toWorldPoint());
            if (local != null && Perspective.localToMinimap(client, local) != null) return tile;
        }
        return null;
    }
}
