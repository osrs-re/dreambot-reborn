package com.dreambotreborn.api.methods.interactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.wrappers.interactive.Player;

/** DreamBot-style queries over players in the loaded scene. */
public final class Players
{
    private static volatile List<Player> snapshot = Collections.emptyList();
    private static volatile Player local;

    private Players()
    {
    }

    public static Query<Player> all()
    {
        return new Query<>(snapshot);
    }

    public static Query<Player> all(String... names)
    {
        return find(player -> Queries.name(player.name, names));
    }

    public static Query<Player> all(int... ids)
    {
        return find(player -> Queries.id(player.id, ids));
    }

    public static List<Player> all(Predicate<? super Player> predicate)
    {
        return find(predicate).all();
    }

    public static Query<Player> find(Predicate<? super Player> predicate)
    {
        return all().filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static Player getLocal()
    {
        return local;
    }

    public static Player getAtIndex(int index)
    {
        return find(player -> player.index == index).first();
    }

    public static Player closest()
    {
        return closest(player -> !player.local);
    }

    public static Player closest(String... names)
    {
        return closest(player -> Queries.name(player.name, names));
    }

    public static Player closest(int... ids)
    {
        return closest(player -> Queries.id(player.id, ids));
    }

    public static Player closest(Predicate<? super Player> predicate)
    {
        return closest(predicate, local == null ? null : local.worldLocation);
    }

    public static Player closest(Predicate<? super Player> predicate, WorldPoint origin)
    {
        Objects.requireNonNull(predicate, "predicate");
        Player result = null;
        int distance = Integer.MAX_VALUE;
        for (Player player : snapshot)
        {
            if (predicate.test(player))
            {
                int candidate = player.distance(origin);
                if (result == null || candidate < distance)
                {
                    result = player;
                    distance = candidate;
                }
            }
        }
        return result;
    }

    public static void refresh()
    {
        Client client = DreamBotRebornApi.requireClient();
        net.runelite.api.Player liveLocal = client.getLocalPlayer();
        String[] actions = client.getPlayerOptions();
        List<Player> players = new ArrayList<>();
        Player localPlayer = null;
        List<net.runelite.api.Player> livePlayers = client.getPlayers();
        if (livePlayers != null)
        {
            for (net.runelite.api.Player live : livePlayers)
            {
                if (live == null)
                {
                    continue;
                }
                Player player = new Player(live, actions, live == liveLocal);
                players.add(player);
                if (live == liveLocal)
                {
                    localPlayer = player;
                }
            }
        }
        if (localPlayer == null && liveLocal != null)
        {
            localPlayer = new Player(liveLocal, actions, true);
            players.add(localPlayer);
        }
        local = localPlayer;
        snapshot = Collections.unmodifiableList(players);
    }

    public static void clear()
    {
        local = null;
        snapshot = Collections.emptyList();
    }
}
