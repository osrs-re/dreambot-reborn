package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.listener.SpawnListener;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;

public class EntitySpawnEvent extends SpawnEvent<Entity>
{
    public EntitySpawnEvent(Entity entity, boolean spawned) { super(entity, spawned); }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof SpawnListener)) return;
        SpawnListener target = (SpawnListener) listener;
        Entity entity = getEntity();
        if (entity instanceof NPC)
        {
            if (spawned) target.onNpcSpawn((NPC) entity); else target.onNpcDespawn((NPC) entity);
        }
        else if (entity instanceof Player)
        {
            Player player = (Player) entity;
            if (spawned)
            {
                target.onPlayerSpawn(player);
                if (player.isLocal()) target.onLocalPlayerSpawn(player);
            }
            else target.onPlayerDespawn(player);
        }
    }
}
