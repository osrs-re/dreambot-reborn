package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.GameTickListener;

public class TickEvent extends ScriptEvent
{
    private final boolean preTick;
    public TickEvent(boolean preTick) { this.preTick = preTick; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof GameTickListener)
        {
            if (preTick) ((GameTickListener) listener).onPreTick();
            else ((GameTickListener) listener).onGameTick();
        }
    }
    @Override public String toString() { return preTick ? "PreTickEvent" : "TickEvent"; }
}
