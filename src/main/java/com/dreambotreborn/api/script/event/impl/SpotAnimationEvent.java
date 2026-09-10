package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.AnimationListener;
import com.dreambotreborn.api.wrappers.graphics.SpotAnimation;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;

public class SpotAnimationEvent extends ScriptEvent
{
    private final Entity entity;
    private final SpotAnimation animation;
    public SpotAnimationEvent(Entity entity, SpotAnimation animation)
    { this.entity = entity; this.animation = animation; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof AnimationListener) || animation == null) return;
        if (entity instanceof Player)
            ((AnimationListener) listener).onPlayerSpotAnimation((Player) entity, animation);
        else if (entity instanceof NPC)
            ((AnimationListener) listener).onNPCSpotAnimation((NPC) entity, animation);
    }
}
