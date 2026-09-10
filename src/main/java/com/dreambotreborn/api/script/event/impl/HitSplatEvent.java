package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.HitSplatListener;
import com.dreambotreborn.api.wrappers.interactive.Entity;

public class HitSplatEvent extends ScriptEvent
{
    private final Entity entity;
    private final int type, value, endCycle, secondaryType, secondaryValue;
    public HitSplatEvent(Entity entity, int type, int value, int endCycle,
                         int secondaryType, int secondaryValue)
    {
        this.entity = entity; this.type = type; this.value = value; this.endCycle = endCycle;
        this.secondaryType = secondaryType; this.secondaryValue = secondaryValue;
    }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof HitSplatListener && entity != null)
            ((HitSplatListener) listener).onHitSplatAdded(entity, type, value, endCycle,
                secondaryType, secondaryValue);
    }
}
