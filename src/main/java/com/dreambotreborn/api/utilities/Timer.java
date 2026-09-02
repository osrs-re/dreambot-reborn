package com.dreambotreborn.api.utilities;

/** Thread-safe DreamBot-style stopwatch/countdown used by running scripts. */
public class Timer
{
    private final long durationMillis;
    private long startedAtMillis;
    private long endsAtMillis;
    private long totalPausedMillis;
    private long pausedAtMillis;
    private long pausedRemainingMillis;
    private boolean paused;

    public Timer(long durationMillis)
    {
        if (durationMillis < 0L)
        {
            throw new IllegalArgumentException("duration cannot be negative");
        }
        this.durationMillis = durationMillis;
        reset();
    }

    public Timer()
    {
        this(0L);
    }

    public synchronized String formatTime()
    {
        return formatTime(elapsed());
    }

    public synchronized String formatRemainingTime()
    {
        return formatTime(remaining());
    }

    /** Changes this countdown's current deadline without changing reset's original duration. */
    public synchronized void setRunTime(long milliseconds)
    {
        endsAtMillis = System.currentTimeMillis() + milliseconds;
        if (paused)
        {
            pausedRemainingMillis = milliseconds;
        }
    }

    public synchronized boolean finished()
    {
        return remaining() <= 0L;
    }

    public synchronized void start()
    {
        reset();
    }

    public synchronized void pause()
    {
        if (paused)
        {
            return;
        }
        pausedAtMillis = System.currentTimeMillis();
        pausedRemainingMillis = remaining();
        paused = true;
    }

    public synchronized void resume()
    {
        if (!paused)
        {
            return;
        }
        long now = System.currentTimeMillis();
        totalPausedMillis += now - pausedAtMillis;
        endsAtMillis = now + pausedRemainingMillis;
        paused = false;
    }

    public synchronized long remaining()
    {
        return paused ? pausedRemainingMillis : endsAtMillis - System.currentTimeMillis();
    }

    public synchronized boolean isPaused()
    {
        return paused;
    }

    public synchronized long elapsed()
    {
        long now = System.currentTimeMillis();
        long currentPause = paused ? now - pausedAtMillis : 0L;
        return now - startedAtMillis - totalPausedMillis - currentPause;
    }

    public synchronized void reset()
    {
        startedAtMillis = System.currentTimeMillis();
        endsAtMillis = startedAtMillis + durationMillis;
        totalPausedMillis = 0L;
        pausedAtMillis = 0L;
        pausedRemainingMillis = 0L;
        paused = false;
    }

    public int getHourlyRate(int value)
    {
        return (int) (value * 3_600_000.0D / elapsed());
    }

    public long getLongHourlyRate(long value)
    {
        return value * 3_600_000L / elapsed();
    }

    public static String formatTime(long milliseconds)
    {
        long totalSeconds = Math.max(0L, milliseconds) / 1_000L;
        long hours = totalSeconds / 3_600L;
        long minutes = totalSeconds % 3_600L / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
