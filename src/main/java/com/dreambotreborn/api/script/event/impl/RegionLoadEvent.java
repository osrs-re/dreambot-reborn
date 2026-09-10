package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.RegionLoadListener;

public class RegionLoadEvent extends ScriptEvent
{
    private final boolean instanced;
    public RegionLoadEvent(boolean instanced) { this.instanced = instanced; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof RegionLoadListener)
            ((RegionLoadListener) listener).onRegionLoad(instanced);
    }
}
