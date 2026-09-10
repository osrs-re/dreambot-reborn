package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.GameTickListener;

public class ServerTickEvent extends ScriptEvent
{
    public ServerTickEvent() { }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof GameTickListener) ((GameTickListener) listener).onServerTick();
    }
    @Override public String toString() { return "ServerTickEvent"; }
}
