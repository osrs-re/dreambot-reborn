package com.dreambotreborn.api.utilities;

import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.utilities.impl.Condition;

/** Interruptible script sleep helpers. */
public final class Sleep
{
    private static volatile long defaultPoll = 50L;

    private Sleep()
    {
    }

    public static void sleep(long milliseconds)
    {
        if (milliseconds <= 0L)
        {
            return;
        }
        try
        {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException interrupted)
        {
            Thread.currentThread().interrupt();
        }
    }

    public static void sleep(long minimum, long maximum)
    {
        if (maximum <= minimum)
        {
            sleep(minimum);
            return;
        }
        sleep(ThreadLocalRandom.current().nextLong(minimum, maximum));
    }

    public static boolean sleepUntil(Condition condition, long timeout)
    {
        return sleepUntil(condition, timeout, defaultPoll);
    }

    public static boolean sleepUntil(Condition condition, long timeout, long poll)
    {
        return sleepUntil(condition, null, timeout, poll);
    }

    public static boolean sleepUntil(
        Condition condition,
        Condition resetCondition,
        long timeout,
        long poll)
    {
        if (condition == null)
        {
            return false;
        }
        long timeoutNanos = Math.max(0L, timeout) * 1_000_000L;
        long deadline = System.nanoTime() + timeoutNanos;
        do
        {
            if (condition.verify())
            {
                return true;
            }
            if (Thread.currentThread().isInterrupted())
            {
                return false;
            }
            sleep(Math.max(1L, poll));
            if (resetCondition != null && resetCondition.verify())
            {
                deadline = System.nanoTime() + timeoutNanos;
            }
        }
        while (System.nanoTime() < deadline);
        return condition.verify();
    }

    public static boolean sleepWhile(Condition condition, long timeout)
    {
        return condition != null && sleepUntil(condition.negate(), timeout);
    }

    public static boolean sleepWhile(Condition condition, long timeout, long poll)
    {
        return condition != null && sleepUntil(condition.negate(), timeout, poll);
    }

    public static boolean sleepWhile(
        Condition condition,
        Condition resetCondition,
        long timeout,
        long poll)
    {
        return condition != null && sleepUntil(condition.negate(), resetCondition, timeout, poll);
    }

    public static void sleepTick()
    {
        sleep(600L);
    }

    public static void sleepTicks(int ticks)
    {
        sleep(Math.max(0, ticks) * 600L);
    }

    public static long getDefaultPoll()
    {
        return defaultPoll;
    }

    public static void setDefaultPoll(long milliseconds)
    {
        if (milliseconds <= 0L)
        {
            throw new IllegalArgumentException("poll interval must be positive");
        }
        defaultPoll = milliseconds;
    }
}
