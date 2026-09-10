package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.SpawnListener;
import com.dreambotreborn.api.wrappers.items.GroundItem;

public class GroundItemSpawnEvent extends ScriptEvent
{
    private final GroundItem item;
    private final boolean spawned;
    private final boolean updated;
    public GroundItemSpawnEvent(GroundItem item, boolean spawned) { this(item, spawned, false); }
    public GroundItemSpawnEvent(GroundItem item, boolean spawned, boolean updated)
    { this.item = item; this.spawned = spawned; this.updated = updated; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof SpawnListener) || item == null) return;
        SpawnListener target = (SpawnListener) listener;
        if (updated) target.onGroundItemUpdate(item);
        else if (spawned) target.onGroundItemSpawn(item); else target.onGroundItemDespawn(item);
    }
    @Override public String toString()
    { return "GroundItemSpawnEvent{" + (updated ? "updated " : spawned ? "spawned " : "despawned ") + item + '}'; }
}
