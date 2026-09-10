package com.dreambotreborn.api.script.event;

import java.util.EventListener;
import java.util.EventObject;

/** Base for client-independent events delivered to scripts. */
public abstract class ScriptEvent extends EventObject
{
    private static final Object EVENT_SOURCE = new Object();
    private final long timestamp;

    public ScriptEvent()
    {
        super(EVENT_SOURCE);
        timestamp = System.currentTimeMillis();
    }

    public abstract void dispatch(EventListener listener);

    public long getTimestamp()
    {
        return timestamp;
    }
}
