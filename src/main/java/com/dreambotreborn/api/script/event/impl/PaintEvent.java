package com.dreambotreborn.api.script.event.impl;

import java.awt.Graphics;
import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.PaintListener;

public class PaintEvent extends ScriptEvent
{
    private Graphics graphics;
    public PaintEvent(Graphics graphics) { this.graphics = graphics; }
    public void setGraphics(Graphics graphics) { this.graphics = graphics; }
    @Override public final void dispatch(EventListener listener)
    {
        if (listener instanceof PaintListener && graphics != null)
            ((PaintListener) listener).onPaint(graphics);
    }
}
