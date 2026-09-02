package com.dreambotreborn.api;

import java.util.Objects;
import net.runelite.api.Client;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.methods.interactive.GraphicsObjects;
import com.dreambotreborn.api.methods.interactive.Projectiles;
import com.dreambotreborn.api.methods.item.GroundItems;
import com.dreambotreborn.api.methods.skills.Skills;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.methods.world.Worlds;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.events.WorldListLoad;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.ProjectileMoved;

/** Entry point that binds the public DreamBot Reborn API to the injected RuneLite client. */
public final class DreamBotRebornApi
{
    private static volatile Client client;

    private DreamBotRebornApi()
    {
    }

    public static synchronized void initialize(Client value)
    {
        client = Objects.requireNonNull(value, "client");
        GameObjects.clear();
        Players.clear();
        NPCs.clear();
        GraphicsObjects.clear();
        Projectiles.clear();
        GroundItems.clear();
        Containers.clear();
        Widgets.clear();
        Skills.clear();
        Worlds.clearWorlds();
    }

    /** Returns the underlying client for functionality not yet wrapped by this API. */
    public static Client getClient()
    {
        return requireClient();
    }

    /** Refreshes all immutable API snapshots. Must be called on the game thread. */
    public static void refresh()
    {
        requireClient();
        GameObjects.refresh();
        Players.refresh();
        NPCs.refresh();
        GraphicsObjects.refresh();
        Projectiles.refresh();
        GroundItems.refresh();
        Containers.refresh();
        Widgets.refresh();
        Skills.refresh();
        Worlds.refresh();
    }

    /** Refreshes only the immutable snapshot affected by a posted client event. */
    public static void onEvent(Object event)
    {
        if (event instanceof ItemContainerChanged)
        {
            Containers.refresh();
        }
        else if (event instanceof GameObjectSpawned || event instanceof GameObjectDespawned
            || event instanceof GroundObjectSpawned || event instanceof GroundObjectDespawned
            || event instanceof WallObjectSpawned || event instanceof WallObjectDespawned
            || event instanceof DecorativeObjectSpawned || event instanceof DecorativeObjectDespawned)
        {
            GameObjects.refresh();
        }
        else if (event instanceof NpcSpawned || event instanceof NpcDespawned
            || event instanceof NpcChanged)
        {
            NPCs.refresh();
        }
        else if (event instanceof PlayerSpawned || event instanceof PlayerDespawned
            || event instanceof PlayerChanged)
        {
            Players.refresh();
        }
        else if (event instanceof ItemSpawned || event instanceof ItemDespawned
            || event instanceof ItemQuantityChanged)
        {
            GroundItems.refresh();
        }
        else if (event instanceof WidgetLoaded || event instanceof WidgetClosed)
        {
            Widgets.refresh();
        }
        else if (event instanceof StatChanged)
        {
            Skills.refresh();
        }
        else if (event instanceof GraphicsObjectCreated)
        {
            GraphicsObjects.refresh();
        }
        else if (event instanceof ProjectileMoved)
        {
            Projectiles.refresh();
        }
        else if (event instanceof WorldListLoad)
        {
            Worlds.refresh();
        }
    }

    public static Client requireClient()
    {
        Client current = client;
        if (current == null)
        {
            throw new IllegalStateException("DreamBotRebornApi has not been initialized");
        }
        return current;
    }
}
