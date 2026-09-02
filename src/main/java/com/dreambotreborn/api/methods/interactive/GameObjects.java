package com.dreambotreborn.api.methods.interactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.wrappers.interactive.GameObject;

/** Queries scenery objects in the current top-level scene and plane. */
public final class GameObjects
{
    private static volatile List<GameObject> snapshot = Collections.emptyList();

    private GameObjects()
    {
    }

    /** @deprecated initialize all APIs through {@link DreamBotRebornApi#initialize(Client)}. */
    @Deprecated
    public static synchronized void initialize(Client value)
    {
        DreamBotRebornApi.initialize(value);
    }

    public static Query<GameObject> find(Predicate<? super GameObject> predicate)
    {
        return all().filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static Query<GameObject> all()
    {
        return new Query<>(snapshot);
    }

    /** DreamBot-style name filtering while retaining the chainable Query result. */
    public static Query<GameObject> all(String... names)
    {
        return find(object -> matchesName(object.name, names));
    }

    /** DreamBot-style id filtering while retaining the chainable Query result. */
    public static Query<GameObject> all(int... ids)
    {
        return find(object -> matchesId(object.id, ids));
    }

    public static GameObject[] getObjectsOnTile(com.dreambotreborn.api.methods.map.Tile tile)
    {
        if (tile == null)
        {
            return new GameObject[0];
        }
        return find(object -> tile.equals(object.getTile())).all().toArray(new GameObject[0]);
    }

    public static GameObject getTopObjectOnTile(com.dreambotreborn.api.methods.map.Tile tile)
    {
        GameObject[] objects = getObjectsOnTile(tile);
        return objects.length == 0 ? null : objects[objects.length - 1];
    }

    public static GameObject closest()
    {
        return closest(object -> true);
    }

    public static GameObject closest(String... names)
    {
        return closest(object -> matchesName(object.name, names));
    }

    public static GameObject closest(int... ids)
    {
        return closest(object -> matchesId(object.id, ids));
    }

    public static GameObject closest(Predicate<? super GameObject> predicate)
    {
        return closest(predicate, localPlayerLocation());
    }

    public static GameObject closest(Predicate<? super GameObject> predicate,
                                     net.runelite.api.coords.WorldPoint origin)
    {
        Objects.requireNonNull(predicate, "predicate");
        GameObject closest = null;
        int closestDistance = Integer.MAX_VALUE;
        for (GameObject object : snapshot)
        {
            if (!predicate.test(object))
            {
                continue;
            }
            int distance = object.distance(origin);
            if (closest == null || distance < closestDistance)
            {
                closest = object;
                closestDistance = distance;
            }
        }
        return closest;
    }

    public static Query<GameObject> named(String name)
    {
        Objects.requireNonNull(name, "name");
        return find(object -> object.name.equals(name));
    }

    public static Query<GameObject> namedIgnoreCase(String name)
    {
        Objects.requireNonNull(name, "name");
        return find(object -> object.name.equalsIgnoreCase(name));
    }

    public static Query<GameObject> withId(int id)
    {
        return find(object -> object.id == id);
    }

    public static Query<GameObject> withAction(String action)
    {
        Objects.requireNonNull(action, "action");
        return find(object -> object.actions.contains(action));
    }

    /**
     * Rebuilds the immutable query snapshot. This must be invoked on the game
     * thread; the launcher does that from its callbacks.
     */
    public static void refresh()
    {
        Client currentClient = DreamBotRebornApi.requireClient();

        WorldView worldView = currentClient.getTopLevelWorldView();
        Scene scene = worldView == null ? null : worldView.getScene();
        if (scene == null)
        {
            snapshot = Collections.emptyList();
            return;
        }

        Tile[][][] tiles = scene.getTiles();
        int plane = worldView.getPlane();
        if (tiles == null || plane < 0 || plane >= tiles.length || tiles[plane] == null)
        {
            snapshot = Collections.emptyList();
            return;
        }

        List<GameObject> objects = new ArrayList<>();
        Set<TileObject> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Tile[] column : tiles[plane])
        {
            if (column == null)
            {
                continue;
            }

            for (Tile tile : column)
            {
                if (tile == null)
                {
                    continue;
                }

                add(currentClient, objects, seen, tile.getWallObject(), GameObject.Type.WALL);
                add(currentClient, objects, seen, tile.getDecorativeObject(), GameObject.Type.DECORATIVE);
                add(currentClient, objects, seen, tile.getGroundObject(), GameObject.Type.GROUND);

                net.runelite.api.GameObject[] tileObjects = tile.getGameObjects();
                if (tileObjects != null)
                {
                    for (net.runelite.api.GameObject tileObject : tileObjects)
                    {
                        add(currentClient, objects, seen, tileObject, GameObject.Type.GAME);
                    }
                }
            }
        }

        snapshot = Collections.unmodifiableList(objects);
    }

    public static void clear()
    {
        snapshot = Collections.emptyList();
    }

    private static net.runelite.api.coords.WorldPoint localPlayerLocation()
    {
        net.runelite.api.Player local = DreamBotRebornApi.requireClient().getLocalPlayer();
        return local == null ? null : local.getWorldLocation();
    }

    private static boolean matchesName(String value, String... names)
    {
        if (names == null)
        {
            return false;
        }
        for (String name : names)
        {
            if (name != null && value.equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesId(int value, int... ids)
    {
        if (ids != null)
        {
            for (int id : ids)
            {
                if (value == id)
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static void add(
        Client currentClient,
        List<GameObject> objects,
        Set<TileObject> seen,
        TileObject object,
        GameObject.Type type)
    {
        if (object == null || !seen.add(object))
        {
            return;
        }

        ObjectComposition composition = currentClient.getObjectDefinition(object.getId());
        if (composition != null && composition.getImpostorIds() != null)
        {
            ObjectComposition transformed = composition.getImpostor();
            if (transformed != null)
            {
                composition = transformed;
            }
        }

        objects.add(new GameObject(object, composition, type));
    }
}
