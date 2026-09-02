package com.dreambotreborn.api.methods;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/** Random-number helpers with deterministic seeded variants for script behavior profiles. */
public final class Randoms
{
    private static volatile long seed = System.nanoTime();
    private static volatile Random random = new Random(seed);

    private Randoms() { }

    public static int random(int maximum)
    {
        return maximum <= 0 ? 0 : next().nextInt(maximum);
    }
    public static int random(int minimum, int maximum)
    {
        int low = Math.min(minimum, maximum);
        int high = Math.max(minimum, maximum);
        return low == high ? low : low + next().nextInt(high - low + 1);
    }
    public static double random(double minimum, double maximum)
    {
        double low = Math.min(minimum, maximum);
        double high = Math.max(minimum, maximum);
        return low + next().nextDouble() * (high - low);
    }
    public static long random(long minimum, long maximum)
    {
        long low = Math.min(minimum, maximum);
        long high = Math.max(minimum, maximum);
        if (low == high) return low;
        double value = next().nextDouble() * ((double) high - low + 1.0);
        return low + Math.min(high - low, (long) value);
    }
    public static double nextGaussianRandom(double mean, double deviation)
    {
        if (!Double.isFinite(deviation) || deviation < 0.0)
            throw new IllegalArgumentException("deviation cannot be negative");
        return mean + next().nextGaussian() * deviation;
    }
    public static double nextLogNormalDistributionRandom(double mean, double deviation)
    {
        if (deviation <= 0.0) throw new IllegalArgumentException("deviation must be positive");
        return Math.exp(nextGaussianRandom(mean, deviation));
    }
    public static double nextHypergeometricRandom(int population, int successes, int draws)
    {
        if (population <= 0 || successes < 0 || successes > population || draws < 0)
            throw new IllegalArgumentException("invalid hypergeometric parameters");
        int remainingPopulation = population;
        int remainingSuccesses = successes;
        int selected = 0;
        for (int index = 0; index < Math.min(draws, population); index++)
        {
            if (next().nextDouble() < remainingSuccesses / (double) remainingPopulation)
            {
                selected++;
                remainingSuccesses--;
            }
            remainingPopulation--;
        }
        return selected;
    }
    public static double nextGammaRandom(int shape, int scale)
    {
        if (shape <= 0 || scale <= 0) throw new IllegalArgumentException("shape and scale must be positive");
        double sum = 0.0;
        for (int index = 0; index < shape; index++)
            sum -= Math.log(Math.max(Double.MIN_VALUE, next().nextDouble()));
        return sum * scale;
    }
    public static synchronized void setSeed(long value)
    {
        seed = value;
        random = new Random(value);
    }
    public static void setSeed(String value) { setSeed(hash(value)); }
    public static Random getSecureRandom() { return next(); }
    public static boolean chance(int numerator, int denominator)
    {
        if (denominator <= 0 || numerator <= 0) return false;
        if (numerator >= denominator) return true;
        return random(1, denominator) <= numerator;
    }
    public static boolean chance(int percent) { return chance(percent, 100); }
    public static int getSeededInt(String key, int minimum, int maximum)
    {
        int low = Math.min(minimum, maximum);
        int high = Math.max(minimum, maximum);
        if (low == high) return low;
        return low + new Random(seed ^ hash(key)).nextInt(high - low + 1);
    }
    public static double getSeededDouble(String key, double minimum, double maximum)
    {
        double low = Math.min(minimum, maximum);
        double high = Math.max(minimum, maximum);
        return low + new Random(seed ^ hash(key)).nextDouble() * (high - low);
    }

    private static synchronized Random next() { return random; }

    private static long hash(String value)
    {
        try
        {
            byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(String.valueOf(value).getBytes(StandardCharsets.UTF_8));
            long result = 0L;
            for (int index = 0; index < Long.BYTES; index++)
                result = (result << 8) | (bytes[index] & 0xffL);
            return result;
        }
        catch (NoSuchAlgorithmException impossible)
        {
            return String.valueOf(value).hashCode();
        }
    }
}
