package com.dreambotreborn.api.methods.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.wrappers.items.GroundItem;

/** DreamBot-style queries over visible ground items. */
public final class GroundItems
{
    private static volatile List<GroundItem> snapshot = Collections.emptyList();

    private GroundItems()
    {
    }

    public static Query<GroundItem> all()
    {
        return new Query<>(snapshot);
    }

    public static Query<GroundItem> all(String... names)
    {
        return find(item -> Queries.name(item.name, names));
    }

    public static Query<GroundItem> all(int... ids)
    {
        return find(item -> Queries.id(item.id, ids));
    }

    public static Query<GroundItem> find(Predicate<? super GroundItem> predicate)
    {
        return all().filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static Query<GroundItem> getForTile(WorldPoint tile)
    {
        return find(item -> Objects.equals(item.worldLocation, tile));
    }

    public static GroundItem closest()
    {
        return closest(item -> true);
    }

    public static GroundItem closest(String... names)
    {
        return closest(item -> Queries.name(item.name, names));
    }

    public static GroundItem closest(int... ids)
    {
        return closest(item -> Queries.id(item.id, ids));
    }

    public static GroundItem closest(Predicate<? super GroundItem> predicate)
    {
        Player local = Players.getLocal();
        return closest(predicate, local == null ? null : local.worldLocation);
    }

    public static GroundItem closest(Predicate<? super GroundItem> predicate, WorldPoint origin)
    {
        Objects.requireNonNull(predicate, "predicate");
        GroundItem result = null;
        int distance = Integer.MAX_VALUE;
        for (GroundItem item : snapshot)
        {
            if (predicate.test(item))
            {
                int candidate = item.distance(origin);
                if (result == null || candidate < distance)
                {
                    result = item;
                    distance = candidate;
                }
            }
        }
        return result;
    }

    public static void refresh()
    {
        Client client = DreamBotRebornApi.requireClient();
        WorldView view = client.getTopLevelWorldView();
        Scene scene = view == null ? null : view.getScene();
        if (scene == null)
        {
            clear();
            return;
        }
        Tile[][][] tiles = scene.getTiles();
        int plane = view.getPlane();
        if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null)
        {
            clear();
            return;
        }

        List<GroundItem> items = new ArrayList<>();
        Set<TileItem> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Tile[] column : tiles[plane])
        {
            if (column == null)
            {
                continue;
            }
            for (Tile tile : column)
            {
                if (tile == null || tile.getGroundItems() == null)
                {
                    continue;
                }
                for (TileItem item : tile.getGroundItems())
                {
                    if (item == null || !seen.add(item))
                    {
                        continue;
                    }
                    ItemComposition definition = client.getItemDefinition(item.getId());
                    items.add(new GroundItem(item, tile,
                        definition == null ? "" : definition.getName()));
                }
            }
        }
        snapshot = Collections.unmodifiableList(items);
    }

    public static void clear()
    {
        snapshot = Collections.emptyList();
    }
}
