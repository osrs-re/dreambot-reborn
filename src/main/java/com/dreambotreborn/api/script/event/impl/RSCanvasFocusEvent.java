package com.dreambotreborn.api.script.event.impl;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;

public class RSCanvasFocusEvent extends ScriptEvent
{
    private final FocusEvent event;
    public RSCanvasFocusEvent(FocusEvent event) { this.event = event; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof FocusListener) || event == null) return;
        if (event.getID() == FocusEvent.FOCUS_GAINED) ((FocusListener) listener).focusGained(event);
        else if (event.getID() == FocusEvent.FOCUS_LOST) ((FocusListener) listener).focusLost(event);
    }
}
