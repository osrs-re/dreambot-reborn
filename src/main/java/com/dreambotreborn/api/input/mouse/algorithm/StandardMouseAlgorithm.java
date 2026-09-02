package com.dreambotreborn.api.input.mouse.algorithm;

import java.awt.Point;
import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.methods.input.mouse.MouseSettings;

/**
 * Direction/magnitude based movement with acceleration, angular noise,
 * destination-aware deceleration, occasional overshoot, and correction.
 */
public class StandardMouseAlgorithm implements MouseMovementAlgorithm
{
    private double preciseX;
    private double preciseY;
    private double velocityX;
    private double velocityY;
    private double decelRadius;
    private Point overshoot;
    private boolean correcting;
    private boolean initialized;

    @Override
    public void begin(Point start, Point target, MouseSettings settings)
    {
        preciseX = start.x;
        preciseY = start.y;
        velocityX = 0.0;
        velocityY = 0.0;
        decelRadius = Math.max(24.0, gaussian(
            getMeanDecelRadius(), getDecelRadiusDeviation()));
        correcting = false;
        overshoot = createOvershoot(start, target, settings);
        initialized = true;
    }

    @Override
    public Point next(Point current, Point target, double elapsedSeconds)
    {
        if (!initialized)
        {
            begin(current, target, new MouseSettings());
        }
        double elapsed = Math.max(0.004, Math.min(0.05, elapsedSeconds));
        Point movementTarget = overshoot != null && !correcting ? overshoot : target;
        double dx = movementTarget.x - preciseX;
        double dy = movementTarget.y - preciseY;
        double distance = Math.hypot(dx, dy);

        if (overshoot != null && !correcting && distance < Math.max(3.0, speed() * elapsed))
        {
            correcting = true;
            movementTarget = target;
            dx = movementTarget.x - preciseX;
            dy = movementTarget.y - preciseY;
            distance = Math.hypot(dx, dy);
        }
        if (distance <= 1.25)
        {
            preciseX = target.x;
            preciseY = target.y;
            velocityX = 0.0;
            velocityY = 0.0;
            return new Point(target);
        }

        double desiredDirection = Math.atan2(dy, dx);
        double magnitude = Math.hypot(velocityX, velocityY);
        double direction = magnitude < 0.01
            ? desiredDirection : Math.atan2(velocityY, velocityX);
        double difference = normalizeAngle(desiredDirection - direction);
        double maxTurn = getMaxdTheta() * Math.max(0.35, elapsed * 60.0);
        direction += clamp(difference, -maxTurn, maxTurn);

        double distanceNoise = Math.min(1.0, distance / 180.0);
        direction += gaussian(0.0, getAngleDeviationRate() * distanceNoise)
            * Math.sqrt(elapsed * 60.0);

        double acceleration = gaussian(getAccelerationRate(), getAccelerationDeviation());
        magnitude += Math.max(0.0, acceleration) * elapsed;
        double profileMaximum = Math.min(getMaxMagnitude(), speed());
        double desiredMaximum = profileMaximum;
        if (distance < Math.min(decelRadius, getMaxDecelDistance()))
        {
            double fraction = Math.max(0.0, distance / decelRadius);
            desiredMaximum = Math.max(getMinDecelMagnitude(),
                profileMaximum * Math.pow(fraction, getDecayDistanceExponent()));
        }
        if (magnitude > desiredMaximum)
        {
            double decay = Math.pow(1.0 - getDecelDecayRate(), elapsed * 60.0);
            magnitude = Math.max(desiredMaximum, magnitude * decay);
        }
        magnitude = clamp(magnitude, Math.min(getMinMagnitude(), desiredMaximum), desiredMaximum);

        velocityX = Math.cos(direction) * magnitude;
        velocityY = Math.sin(direction) * magnitude;
        double stepX = velocityX * elapsed;
        double stepY = velocityY * elapsed;
        if (correcting && Math.hypot(stepX, stepY) >= distance)
        {
            preciseX = target.x;
            preciseY = target.y;
        }
        else
        {
            preciseX += stepX;
            preciseY += stepY;
        }
        return new Point((int) Math.round(preciseX), (int) Math.round(preciseY));
    }

    @Override
    public boolean isComplete(Point current, Point target)
    {
        return (overshoot == null || correcting)
            && MouseMovementAlgorithm.super.isComplete(current, target);
    }

    @Override
    public void reset()
    {
        initialized = false;
        overshoot = null;
        correcting = false;
        velocityX = 0.0;
        velocityY = 0.0;
    }

    private Point createOvershoot(Point start, Point target, MouseSettings settings)
    {
        double distance = start.distance(target);
        if (settings == null || !settings.isOvershoot() || distance < 130.0
            || ThreadLocalRandom.current().nextDouble() > 0.24)
        {
            return null;
        }
        double angle = Math.atan2(target.y - start.y, target.x - start.x);
        double amount = Math.min(34.0, Math.max(7.0, distance * random(0.035, 0.09)));
        double side = gaussian(0.0, Math.min(10.0, amount * 0.35));
        return new Point(
            (int) Math.round(target.x + Math.cos(angle) * amount - Math.sin(angle) * side),
            (int) Math.round(target.y + Math.sin(angle) * amount + Math.cos(angle) * side));
    }

    public double getCanvasReportRate() { return 60.0; }
    public double dynamicRandom() { return ThreadLocalRandom.current().nextDouble(); }
    public double getMaxdTheta() { return MouseProfile.getMaxdTheta(); }
    public double getDecelRadiusDeviation() { return MouseProfile.getDecelRadiusDeviation(); }
    public double getDecayDistanceExponent() { return MouseProfile.getDecayDistanceExponent(); }
    public double getMinMagnitude() { return MouseProfile.getMinMagnitude(); }
    public double getAccelerationDeviation() { return MouseProfile.getAccelerationDeviation(); }
    public double getMinDecelMagnitude() { return MouseProfile.getMinDecelMagnitude(); }
    public double getMaxDecelDistance() { return MouseProfile.getMaxDecelDistance(); }
    public double getAccelerationRate() { return MouseProfile.getAccelerationRate(); }
    public double getMaxMagnitude() { return MouseProfile.getMaxMagnitude(); }
    public double getDecelDecayRate() { return MouseProfile.getDecelDecayRate(); }
    public double getAngleDeviationRate() { return MouseProfile.getAngleDeviationRate(); }
    public double getMeanDecelRadius() { return MouseProfile.getMeanDecelRadius(); }

    private static double speed()
    {
        return Math.max(MouseProfile.getMinMagnitude(), MouseProfile.getSpeed());
    }

    private static double normalizeAngle(double angle)
    {
        while (angle > Math.PI) angle -= Math.PI * 2.0;
        while (angle < -Math.PI) angle += Math.PI * 2.0;
        return angle;
    }

    private static double clamp(double value, double minimum, double maximum)
    {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double gaussian(double mean, double deviation)
    {
        return deviation <= 0.0 ? mean
            : mean + ThreadLocalRandom.current().nextGaussian() * deviation;
    }

    private static double random(double minimum, double maximum)
    {
        return ThreadLocalRandom.current().nextDouble(minimum, maximum);
    }
}
