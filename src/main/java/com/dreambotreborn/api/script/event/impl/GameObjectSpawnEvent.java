package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.SpawnListener;
import com.dreambotreborn.api.wrappers.interactive.GameObject;

public class GameObjectSpawnEvent extends ScriptEvent
{
    private final GameObject object;
    private final boolean spawned;
    public GameObjectSpawnEvent(GameObject object, boolean spawned)
    { this.object = object; this.spawned = spawned; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof SpawnListener) || object == null) return;
        SpawnListener target = (SpawnListener) listener;
        target.onGameObject(object);
        if (spawned) target.onGameObjectSpawn(object); else target.onGameObjectDespawn(object);
    }
    @Override public String toString()
    { return "GameObjectSpawnEvent{" + (spawned ? "spawned " : "despawned ") + object + '}'; }
}
