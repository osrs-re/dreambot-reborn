package com.dreambotreborn.api.input.mouse.algorithm;

import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.methods.input.mouse.MouseTiming;

/** Global, live-tunable movement profile used by the standard mouse algorithm. */
public final class MouseProfile
{
    private static final double DEFAULT_SPEED = 900.0;
    private static final MouseTiming DEFAULT_TIMING =
        () -> ThreadLocalRandom.current().nextInt(38, 91);

    private static volatile double speed;
    private static volatile MouseTiming mouseTiming;
    private static volatile double minMagnitude;
    private static volatile double accelerationDeviation;
    private static volatile double accelerationRate;
    private static volatile double minDecelMagnitude;
    private static volatile double maxDecelDistance;
    private static volatile double decelDecayRate;
    private static volatile double angleDeviationRate;
    private static volatile double meanDecelRadius;
    private static volatile double decelRadiusDeviation;
    private static volatile double maxdTheta;
    private static volatile double decayDistanceExponent;
    private static volatile double maxMagnitude;
    private static volatile boolean overshootEnabled;

    static
    {
        resetToDefaults();
    }

    private MouseProfile()
    {
    }

    public static void resetToDefaults()
    {
        speed = DEFAULT_SPEED;
        mouseTiming = DEFAULT_TIMING;
        minMagnitude = 70.0;
        accelerationDeviation = 650.0;
        accelerationRate = 4600.0;
        minDecelMagnitude = 45.0;
        maxDecelDistance = 180.0;
        decelDecayRate = 0.18;
        angleDeviationRate = 0.075;
        meanDecelRadius = 105.0;
        decelRadiusDeviation = 24.0;
        maxdTheta = 0.34;
        decayDistanceExponent = 0.72;
        maxMagnitude = 1250.0;
        overshootEnabled = true;
    }

    public static void setSpeed(double value)
    {
        requirePositive(value, "speed");
        speed = value;
    }

    public static double getSpeed()
    {
        return speed;
    }

    public static MouseTiming getDefaultMouseTiming()
    {
        return DEFAULT_TIMING;
    }

    public static MouseTiming getMouseTiming()
    {
        return mouseTiming;
    }

    public static void setMouseTiming(MouseTiming value)
    {
        mouseTiming = value == null ? DEFAULT_TIMING : value;
    }

    public static double getMinMagnitude() { return minMagnitude; }
    public static void setMinMagnitude(double value) { requireNonNegative(value, "minMagnitude"); minMagnitude = value; }
    public static double getAccelerationDeviation() { return accelerationDeviation; }
    public static void setAccelerationDeviation(double value) { requireNonNegative(value, "accelerationDeviation"); accelerationDeviation = value; }
    public static double getAccelerationRate() { return accelerationRate; }
    public static void setAccelerationRate(double value) { requirePositive(value, "accelerationRate"); accelerationRate = value; }
    public static double getMinDecelMagnitude() { return minDecelMagnitude; }
    public static void setMinDecelMagnitude(double value) { requireNonNegative(value, "minDecelMagnitude"); minDecelMagnitude = value; }
    public static double getMaxDecelDistance() { return maxDecelDistance; }
    public static void setMaxDecelDistance(double value) { requirePositive(value, "maxDecelDistance"); maxDecelDistance = value; }
    public static double getDecelDecayRate() { return decelDecayRate; }
    public static void setDecelDecayRate(double value) { requireRange(value, 0.0, 1.0, "decelDecayRate"); decelDecayRate = value; }
    public static double getAngleDeviationRate() { return angleDeviationRate; }
    public static void setAngleDeviationRate(double value) { requireNonNegative(value, "angleDeviationRate"); angleDeviationRate = value; }
    public static double getMeanDecelRadius() { return meanDecelRadius; }
    public static void setMeanDecelRadius(double value) { requirePositive(value, "meanDecelRadius"); meanDecelRadius = value; }
    public static double getDecelRadiusDeviation() { return decelRadiusDeviation; }
    public static void setDecelRadiusDeviation(double value) { requireNonNegative(value, "decelRadiusDeviation"); decelRadiusDeviation = value; }
    public static double getMaxdTheta() { return maxdTheta; }
    public static void setMaxdTheta(double value) { requirePositive(value, "maxdTheta"); maxdTheta = value; }
    public static double getDecayDistanceExponent() { return decayDistanceExponent; }
    public static void setDecayDistanceExponent(double value) { requirePositive(value, "decayDistanceExponent"); decayDistanceExponent = value; }
    public static double getMaxMagnitude() { return maxMagnitude; }
    public static void setMaxMagnitude(double value) { requirePositive(value, "maxMagnitude"); maxMagnitude = value; }
    public static double getDEFAULT_SPEED() { return DEFAULT_SPEED; }
    public static boolean isOvershootEnabled() { return overshootEnabled; }
    public static void setOvershootEnabled(boolean value) { overshootEnabled = value; }

    private static void requirePositive(double value, String name)
    {
        if (!Double.isFinite(value) || value <= 0.0)
        {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requireNonNegative(double value, String name)
    {
        if (!Double.isFinite(value) || value < 0.0)
        {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }

    private static void requireRange(double value, double minimum, double maximum, String name)
    {
        if (!Double.isFinite(value) || value < minimum || value > maximum)
        {
            throw new IllegalArgumentException(name + " must be between " + minimum + " and " + maximum);
        }
    }
}
