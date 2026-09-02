package com.dreambotreborn.api.randoms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.time.Duration;

/** Explicitly scheduled break that pauses ordinary script loops without blocking callbacks. */
public class BreakSolver extends RandomSolver
{
    private volatile long breakUntil;

    public BreakSolver()
    {
        super(RandomEvent.BREAK);
    }

    public void schedule(Duration duration)
    {
        long millis = duration == null ? 0L : Math.max(0L, duration.toMillis());
        breakUntil = System.currentTimeMillis() + millis;
    }

    public void cancel()
    {
        breakUntil = 0L;
    }

    @Override public boolean shouldExecute() { return System.currentTimeMillis() < breakUntil; }
    @Override public int onLoop() { return 500; }
    public boolean isBreakRunning() { return shouldExecute(); }
    public long getRemainingMillis()
    {
        return Math.max(0L, breakUntil - System.currentTimeMillis());
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
        if (!shouldExecute()) return;
        long seconds = Math.max(0L, (breakUntil - System.currentTimeMillis()) / 1000L);
        graphics.setColor(new Color(0, 0, 0, 165));
        graphics.fillRoundRect(8, 8, 180, 30, 8, 8);
        graphics.setColor(Color.WHITE);
        graphics.drawString("Break: " + seconds + "s remaining", 18, 28);
    }
}
