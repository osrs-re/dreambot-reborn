package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;

public interface SpawnListener extends EventListener
{
    default void onGameObjectSpawn(GameObjectSpawned event) { }
    default void onGameObjectDespawn(GameObjectDespawned event) { }
    default void onGroundItemSpawn(ItemSpawned event) { }
    default void onGroundItemDespawn(ItemDespawned event) { }
    default void onGroundItemUpdate(ItemQuantityChanged event) { }
    default void onNpcSpawn(NpcSpawned event) { }
    default void onNpcDespawn(NpcDespawned event) { }
    default void onPlayerSpawn(PlayerSpawned event) { }
    default void onPlayerDespawn(PlayerDespawned event) { }
}
