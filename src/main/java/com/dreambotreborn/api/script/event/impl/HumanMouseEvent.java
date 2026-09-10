package com.dreambotreborn.api.script.event.impl;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.HumanMouseListener;

public class HumanMouseEvent extends ScriptEvent
{
    private final MouseEvent event;
    public HumanMouseEvent(MouseEvent event) { this.event = event; }
    @Override public final void dispatch(EventListener listener)
    {
        if (!(listener instanceof HumanMouseListener) || event == null) return;
        HumanMouseListener target = (HumanMouseListener) listener;
        switch (event.getID())
        {
            case MouseEvent.MOUSE_CLICKED: target.onMouseClicked(event); break;
            case MouseEvent.MOUSE_PRESSED: target.onMousePressed(event); break;
            case MouseEvent.MOUSE_RELEASED: target.onMouseReleased(event); break;
            case MouseEvent.MOUSE_ENTERED: target.onMouseEntered(event); break;
            case MouseEvent.MOUSE_EXITED: target.onMouseExited(event); break;
            case MouseEvent.MOUSE_DRAGGED: target.onMouseDragged(event); break;
            case MouseEvent.MOUSE_MOVED: target.onMouseMoved(event); break;
            case MouseEvent.MOUSE_WHEEL:
                if (event instanceof MouseWheelEvent) target.onMouseWheelMoved((MouseWheelEvent) event);
                break;
            default: break;
        }
    }
}
