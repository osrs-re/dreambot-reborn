package com.dreambotreborn.api.script.event.impl;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;

public class RSCanvasMouseEvent extends ScriptEvent
{
    private final MouseEvent event;
    public RSCanvasMouseEvent(MouseEvent event) { this.event = event; }
    public MouseEvent getEvent() { return event; }
    @Override public void dispatch(EventListener listener)
    {
        if (event == null) return;
        if (listener instanceof MouseListener)
        {
            MouseListener target = (MouseListener) listener;
            switch (event.getID())
            {
                case MouseEvent.MOUSE_CLICKED: target.mouseClicked(event); break;
                case MouseEvent.MOUSE_PRESSED: target.mousePressed(event); break;
                case MouseEvent.MOUSE_RELEASED: target.mouseReleased(event); break;
                case MouseEvent.MOUSE_ENTERED: target.mouseEntered(event); break;
                case MouseEvent.MOUSE_EXITED: target.mouseExited(event); break;
                default: break;
            }
        }
        if (listener instanceof MouseMotionListener)
        {
            if (event.getID() == MouseEvent.MOUSE_MOVED)
                ((MouseMotionListener) listener).mouseMoved(event);
            else if (event.getID() == MouseEvent.MOUSE_DRAGGED)
                ((MouseMotionListener) listener).mouseDragged(event);
        }
        if (listener instanceof MouseWheelListener && event instanceof MouseWheelEvent)
            ((MouseWheelListener) listener).mouseWheelMoved((MouseWheelEvent) event);
    }
}
