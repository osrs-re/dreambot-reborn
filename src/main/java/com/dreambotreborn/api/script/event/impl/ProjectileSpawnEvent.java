package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.listener.SpawnListener;
import com.dreambotreborn.api.wrappers.graphics.Projectile;

public class ProjectileSpawnEvent extends SpawnEvent<Projectile>
{
    public ProjectileSpawnEvent(Projectile projectile) { super(projectile, true); }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof SpawnListener && getEntity() != null)
            ((SpawnListener) listener).onProjectileSpawn(getEntity());
    }
}
