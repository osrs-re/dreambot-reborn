package com.dreambotreborn.settings;

import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.World;
import net.runelite.api.WorldType;

/** Applies a preferred world on the game thread when requested by the settings UI. */
public final class PreferredWorld
{
    private static volatile Client client;
    private static volatile Request request;

    private PreferredWorld()
    {
    }

    public static synchronized void initialize(Client value)
    {
        client = Objects.requireNonNull(value, "client");
        Request previous = request;
        request = null;
        if (previous != null)
        {
            previous.future.complete("World change cancelled because the client restarted");
        }
    }

    public static synchronized CompletableFuture<String> apply(int worldId)
    {
        if (worldId < 301 || worldId > 999)
        {
            return CompletableFuture.completedFuture("Choose a preferred world first");
        }
        if (request != null)
        {
            return CompletableFuture.completedFuture("A world change is already pending");
        }
        Request created = new Request(worldId);
        request = created;
        return created.future;
    }

    /** Called by MinimalCallbacks on the game thread. */
    public static void tick()
    {
        Client currentClient = client;
        Request current = request;
        if (currentClient == null || current == null)
        {
            return;
        }
        if (currentClient.getGameState() != GameState.LOGIN_SCREEN)
        {
            complete(current, "Preferred world can only be changed on the login screen");
            return;
        }
        if (currentClient.getWorld() == current.worldId)
        {
            complete(current, "World " + current.worldId + " is already selected");
            return;
        }

        World target = find(currentClient.getWorldList(), current.worldId);
        if (target == null)
        {
            target = currentClient.createWorld();
            target.setId(current.worldId);
            target.setAddress(worldHost(current.worldId));
            target.setActivity("");
            target.setLocation(0);
            target.setPlayerCount(-1);
            target.setIndex(Math.max(0, current.worldId - 301));
            target.setTypes(EnumSet.noneOf(WorldType.class));
        }
        currentClient.changeWorld(target);
        complete(current, "Selected world " + current.worldId);
    }

    public static String worldHost(int worldId)
    {
        // Jagex world hosts use the numeric world offset without the historical
        // trailing "a" (world 301 -> oldschool1.runescape.com).
        return "oldschool" + (worldId - 300) + ".runescape.com";
    }

    private static World find(World[] worlds, int id)
    {
        if (worlds != null)
        {
            for (World world : worlds)
            {
                if (world != null && world.getId() == id)
                {
                    return world;
                }
            }
        }
        return null;
    }

    private static synchronized void complete(Request expected, String message)
    {
        if (request == expected)
        {
            request = null;
            expected.future.complete(message);
        }
    }

    private static final class Request
    {
        private final int worldId;
        private final CompletableFuture<String> future = new CompletableFuture<>();

        private Request(int worldId)
        {
            this.worldId = worldId;
        }
    }
}
