package com.dreambotreborn.api.script.schedule.conditions;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/** Stops after a newly randomized duration between the configured bounds. */
public class RunningTime implements StopCondition
{
    private String minimumRunningTime = "00:30:00";
    private String maximumRunningTime = "00:45:00";
    private volatile long deadline;

    public RunningTime()
    {
        reset();
    }

    public RunningTime(Duration minimum, Duration maximum)
    {
        setMinimumRunningTime(format(minimum));
        setMaximumRunningTime(format(maximum));
        reset();
    }

    @Override
    public boolean shouldStop()
    {
        return System.nanoTime() >= deadline;
    }

    @Override
    public void reset()
    {
        long minimum = parse(minimumRunningTime).toMillis();
        long maximum = parse(maximumRunningTime).toMillis();
        if (maximum < minimum)
        {
            long swap = minimum;
            minimum = maximum;
            maximum = swap;
        }
        long selected = maximum == minimum ? minimum
            : ThreadLocalRandom.current().nextLong(minimum, maximum + 1);
        deadline = System.nanoTime() + selected * 1_000_000L;
    }

    public void setMinimumRunningTime(String value)
    {
        parse(value);
        minimumRunningTime = value;
    }

    public String getMinimumRunningTime() { return minimumRunningTime; }

    public void setMaximumRunningTime(String value)
    {
        parse(value);
        maximumRunningTime = value;
    }

    public String getMaximumRunningTime() { return maximumRunningTime; }

    private static Duration parse(String value)
    {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException("Duration is required");
        String[] parts = value.trim().split(":");
        if (parts.length < 1 || parts.length > 3)
            throw new IllegalArgumentException("Use HH:mm:ss, mm:ss, or seconds");
        long seconds = 0;
        try
        {
            for (String part : parts) seconds = Math.addExact(Math.multiplyExact(seconds, 60), Long.parseLong(part));
        }
        catch (ArithmeticException ex)
        {
            throw new IllegalArgumentException("Invalid duration: " + value, ex);
        }
        if (seconds < 0) throw new IllegalArgumentException("Duration cannot be negative");
        return Duration.ofSeconds(seconds);
    }

    private static String format(Duration value)
    {
        long seconds = value == null ? 0L : Math.max(0L, value.getSeconds());
        return String.format("%02d:%02d:%02d", seconds / 3600,
            (seconds % 3600) / 60, seconds % 60);
    }
}
