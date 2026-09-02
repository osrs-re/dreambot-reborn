package com.dreambotreborn.api.methods;

import java.awt.Point;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.runelite.api.coords.WorldPoint;

/** Small random, timing, and distance utility collection. */
public final class Calculations
{
    private static volatile Random seeded;

    private Calculations()
    {
    }

    public static int random(int maximumExclusive)
    {
        return randomSource().nextInt(maximumExclusive);
    }

    public static int random(int minimumInclusive, int maximumExclusive)
    {
        if (maximumExclusive <= minimumInclusive)
        {
            return minimumInclusive;
        }
        return minimumInclusive + randomSource().nextInt(maximumExclusive - minimumInclusive);
    }

    public static long random(long minimumInclusive, long maximumExclusive)
    {
        if (maximumExclusive <= minimumInclusive)
        {
            return minimumInclusive;
        }
        long bound = maximumExclusive - minimumInclusive;
        long candidate = Math.floorMod(randomSource().nextLong(), bound);
        return minimumInclusive + candidate;
    }

    public static double random(double minimumInclusive, double maximumExclusive)
    {
        return minimumInclusive + randomSource().nextDouble() * (maximumExclusive - minimumInclusive);
    }

    /** Gaussian value constrained to one deviation either side of the mean. */
    public static double nextGaussianRandom(double mean, double deviation)
    {
        if (deviation <= 0.0D)
        {
            return mean;
        }
        double value;
        do
        {
            value = mean + randomSource().nextGaussian() * deviation;
        }
        while (value >= mean + deviation || value <= mean - deviation);
        return value;
    }

    public static double distance(WorldPoint first, WorldPoint second)
    {
        return first == null || second == null ? Double.POSITIVE_INFINITY
            : first.distanceTo2D(second);
    }

    public static double distance(Point first, Point second)
    {
        return first == null || second == null ? Double.POSITIVE_INFINITY
            : first.distance(second);
    }

    public static double distance(int x1, int y1, int x2, int y2)
    {
        return Point.distance(x1, y1, x2, y2);
    }

    public static boolean isBefore(long timestamp)
    {
        return System.currentTimeMillis() < timestamp;
    }

    public static long elapsed(long timestamp)
    {
        return Math.max(0L, System.currentTimeMillis() - timestamp);
    }

    public static boolean chance(int percentage)
    {
        return chance(percentage, 100);
    }

    public static boolean chance(int numerator, int denominator)
    {
        return denominator > 0 && numerator > 0
            && random(denominator) < Math.min(numerator, denominator);
    }

    public static void setRandomSeed(long seed)
    {
        seeded = new Random(seed);
    }

    public static Random getRandom()
    {
        return randomSource();
    }

    private static Random randomSource()
    {
        Random configured = seeded;
        return configured == null ? ThreadLocalRandom.current() : configured;
    }
}
