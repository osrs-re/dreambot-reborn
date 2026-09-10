package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.graphics.Projectile;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.wrappers.items.GroundItem;
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
    default void onGameObject(GameObject object) { }
    default void onGameObjectSpawn(GameObject object) { }
    default void onGameObjectDespawn(GameObject object) { }
    default void onGroundItemSpawn(GroundItem item) { }
    default void onGroundItemDespawn(GroundItem item) { }
    default void onGroundItemUpdate(GroundItem item) { }
    default void onNpcSpawn(NPC npc) { }
    default void onNpcDespawn(NPC npc) { }
    default void onPlayerSpawn(Player player) { }
    default void onLocalPlayerSpawn(Player player) { }
    default void onPlayerDespawn(Player player) { }
    default void onProjectileSpawn(Projectile projectile) { }

    /** RuneLite-native callbacks retained for lower-level integrations. */
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
