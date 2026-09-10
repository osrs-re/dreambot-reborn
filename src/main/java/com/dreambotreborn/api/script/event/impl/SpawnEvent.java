package com.dreambotreborn.api.script.event.impl;

import com.dreambotreborn.api.script.event.ScriptEvent;

public abstract class SpawnEvent<T> extends ScriptEvent
{
    private final T entity;
    protected final boolean spawned;
    public SpawnEvent(T entity, boolean spawned) { this.entity = entity; this.spawned = spawned; }
    public T getEntity() { return entity; }
    @Override public String toString()
    {
        return getClass().getSimpleName() + "{" + (spawned ? "spawned " : "despawned ") + entity + '}';
    }
}
