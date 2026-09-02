package com.dreambotreborn.api.randoms;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EventListener;
import com.dreambotreborn.api.script.listener.PaintListener;

/** Interrupt handler that temporarily takes priority over the normal script loop. */
public abstract class RandomSolver implements PaintListener, EventListener
{
    private final String eventString;
    private volatile boolean enabled = true;
    private volatile boolean forceDisable;
    private volatile long lastRan;
    private volatile int minimumRest = 250;

    protected RandomSolver(RandomEvent event)
    {
        this(event == null ? "CUSTOM" : event.name());
    }

    protected RandomSolver(String eventString)
    {
        this.eventString = eventString == null ? "CUSTOM" : eventString;
    }

    public String getDebugInfo() { return eventString; }
    public abstract boolean shouldExecute();
    public void onStart() { }
    public abstract int onLoop();
    public void onFinish() { }
    public String getEventString() { return eventString; }
    @Override public void onPaint(Graphics graphics) { PaintListener.super.onPaint(graphics); }
    @Override public void onPaint(Graphics2D graphics) { }
    public void disable() { enabled = false; }
    public void enable() { enabled = true; }
    public boolean isEnabled() { return enabled && !forceDisable; }
    public long lastRan() { return lastRan; }
    void markRan() { lastRan = System.currentTimeMillis(); }
    public boolean isForceDisable() { return forceDisable; }
    public void setForceDisable(boolean value) { forceDisable = value; }
    public int getMinimumRest() { return minimumRest; }
    public void setMinimumRest(int value) { minimumRest = Math.max(0, value); }
}
