package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.methods.cs2.RuneScriptEvent;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.RSScriptEventListener;
import com.dreambotreborn.api.script.listener.RuneScriptListener;

public class ScriptPrefiredEvent extends ScriptEvent
{
    private final RuneScriptEvent event;
    public ScriptPrefiredEvent(RuneScriptEvent event) { this.event = event; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof RuneScriptListener)
            ((RuneScriptListener) listener).onPrefired(this);
        if (listener instanceof RSScriptEventListener)
        {
            ((RSScriptEventListener) listener).preFired(event);
            ((RSScriptEventListener) listener).onPreFired(event);
        }
    }
}
