package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.ProjectileListener;
import com.dreambotreborn.api.wrappers.graphics.Projectile;

public class ProjectileTargetEvent extends ScriptEvent
{
    private final Projectile projectile;
    private final boolean before;
    public ProjectileTargetEvent(Projectile projectile, boolean before)
    { this.projectile = projectile; this.before = before; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof ProjectileListener) || projectile == null) return;
        ProjectileListener target = (ProjectileListener) listener;
        if (before) target.preTargeted(projectile);
        target.onTargeted(projectile, projectile.getTargetTile());
        if (!before) target.postTargeted(projectile);
    }
}
