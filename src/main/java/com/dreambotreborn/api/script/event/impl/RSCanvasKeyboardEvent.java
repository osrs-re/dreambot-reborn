package com.dreambotreborn.api.script.event.impl;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.KeyboardListener;

public class RSCanvasKeyboardEvent extends ScriptEvent
{
    private final KeyEvent event;
    public RSCanvasKeyboardEvent(KeyEvent event) { this.event = event; }
    public KeyEvent getEvent() { return event; }
    @Override public void dispatch(EventListener listener)
    {
        if (event == null) return;
        if (listener instanceof KeyboardListener)
        {
            KeyboardListener target = (KeyboardListener) listener;
            if (event.getID() == KeyEvent.KEY_PRESSED) target.onKeyPressed(event);
            else if (event.getID() == KeyEvent.KEY_RELEASED) target.onKeyReleased(event);
            else if (event.getID() == KeyEvent.KEY_TYPED) target.onKeyTyped(event);
        }
        if (listener instanceof KeyListener)
        {
            KeyListener target = (KeyListener) listener;
            if (event.getID() == KeyEvent.KEY_PRESSED) target.keyPressed(event);
            else if (event.getID() == KeyEvent.KEY_RELEASED) target.keyReleased(event);
            else if (event.getID() == KeyEvent.KEY_TYPED) target.keyTyped(event);
        }
    }
}
