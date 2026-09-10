package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.VarListener;

public class VarBitEvent extends ScriptEvent
{
    private final int id, previous, current;
    public VarBitEvent(int id, int previous, int current)
    { this.id = id; this.previous = previous; this.current = current; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof VarListener)
        {
            ((VarListener) listener).onVarBitUpdate(id, previous, current);
            ((VarListener) listener).onVarBitUpdate(id, current);
        }
    }
}
