package com.dreambotreborn.api.methods.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;

/** Current RuneScape world-list queries. */
public final class Worlds
{
    private static volatile List<World> snapshot = Collections.emptyList();

    private Worlds()
    {
    }

    public static int getCurrentWorld()
    {
        return DreamBotRebornApi.requireClient().getWorld();
    }

    public static Query<World> all()
    {
        return new Query<>(snapshot);
    }

    public static Query<World> all(Predicate<? super World> predicate)
    {
        return all().filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static Query<World> f2p()
    {
        return all(World::isF2P);
    }

    public static Query<World> members()
    {
        return all(World::isMembers);
    }

    public static Query<World> pvp()
    {
        return all(World::isPVP);
    }

    public static Query<World> highRisk()
    {
        return all(World::isHighRisk);
    }

    public static World getWorld(int id)
    {
        return all(world -> world.id == id).first();
    }

    public static World getWorld(Predicate<? super World> predicate)
    {
        return all(predicate).first();
    }

    public static World getRandomWorld()
    {
        return all().random();
    }

    public static World getRandomWorld(Predicate<? super World> predicate)
    {
        return all(predicate).random();
    }

    public static World getCurrent()
    {
        return getWorld(getCurrentWorld());
    }

    public static World getMyWorld()
    {
        return getCurrent();
    }

    public static void refresh()
    {
        net.runelite.api.World[] worlds = DreamBotRebornApi.requireClient().getWorldList();
        if (worlds == null)
        {
            snapshot = Collections.emptyList();
            return;
        }
        List<World> result = new ArrayList<>();
        for (net.runelite.api.World world : worlds)
        {
            if (world != null)
            {
                result.add(new World(world));
            }
        }
        snapshot = Collections.unmodifiableList(result);
    }

    public static void clearWorlds()
    {
        snapshot = Collections.emptyList();
    }
}
