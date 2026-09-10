package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.BreakListener;

public class BreakEvent extends ScriptEvent
{
    private final boolean start;
    private final boolean afk;
    private final long endTime;
    public BreakEvent(boolean start, boolean afk, long timeLeftInMs)
    { this.start = start; this.afk = afk; this.endTime = System.currentTimeMillis() + Math.max(0, timeLeftInMs); }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof BreakListener)) return;
        if (start) ((BreakListener) listener).onBreakStart(this);
        else ((BreakListener) listener).onBreakEnd(this);
    }
    public boolean isAfk() { return afk; }
    public long getTimeLeftInMS() { return Math.max(0, endTime - System.currentTimeMillis()); }
}
