package com.dreambotreborn.api.methods.walking.pathfinding.impl;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.path.impl.LocalPath;
import com.dreambotreborn.api.methods.walking.pathfinding.impl.local.LocalPathFinder;
import net.runelite.api.CollisionData;

/** Entry point for collision-safe paths inside the currently loaded scene. */
public final class PathFinder
{
    private static final LocalPathFinder LOCAL = LocalPathFinder.getLocalPathFinder();

    private PathFinder()
    {
    }

    public static CompletableFuture<LocalPath> findPath(Tile destination)
    {
        return ClientThread.invoke(() -> findNow(destination));
    }

    public static LocalPath findNow(Tile destination)
    {
        net.runelite.api.Player player = DreamBotRebornApi.requireClient().getLocalPlayer();
        if (player == null || destination == null) return new LocalPath(Collections.emptyList());
        Tile start = new Tile(player.getWorldLocation());
        int plane = DreamBotRebornApi.requireClient().getPlane();
        CollisionData[] maps = DreamBotRebornApi.requireClient().getCollisionMaps();
        if (maps == null || plane < 0 || plane >= maps.length || maps[plane] == null)
            return new LocalPath(Collections.emptyList());
        int[][] flags = maps[plane].getFlags();
        List<Tile> tiles = LOCAL.find(flags, DreamBotRebornApi.requireClient().getBaseX(),
            DreamBotRebornApi.requireClient().getBaseY(), plane, start, destination);
        return new LocalPath(tiles);
    }

    public static LocalPath findNow(Tile start, Tile destination)
    {
        return LocalPathFinder.getLocalPathFinder().calculate(start, destination);
    }
}
