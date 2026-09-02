package com.dreambotreborn.api.input.mouse.destination;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.utilities.impl.Condition;

/** A live target shape plus optional preparation and verification hooks. */
public abstract class AbstractMouseDestination<T>
{
    private final T target;
    private Rectangle containmentBounds;
    private Condition interrupt;

    protected AbstractMouseDestination(T target)
    {
        this.target = target;
    }

    public T getTarget()
    {
        return target;
    }

    public abstract Shape getDestinationShape();

    public Rectangle getBoundingBox()
    {
        Shape shape = getDestinationShape();
        return shape == null ? null : shape.getBounds();
    }

    public boolean handleCamera() { return handleCamera(false, null); }
    public boolean handleCamera(boolean async, Condition condition) { return false; }
    public boolean handleWalk() { return false; }

    public Area getArea()
    {
        Shape shape = getDestinationShape();
        return shape == null ? new Area() : new Area(shape);
    }

    public Rectangle getContainmentBounds()
    {
        return containmentBounds == null ? null : new Rectangle(containmentBounds);
    }

    public void setContainmentBounds(Rectangle bounds)
    {
        containmentBounds = bounds == null ? null : new Rectangle(bounds);
    }

    public abstract boolean isVisible();

    public boolean valid()
    {
        Condition currentInterrupt = interrupt;
        Shape shape = getDestinationShape();
        return (currentInterrupt == null || !currentInterrupt.verify())
            && shape != null && !shape.getBounds().isEmpty();
    }

    public int type() { return 0; }

    public Point getCenterPoint()
    {
        Rectangle bounds = getBoundingBox();
        return bounds == null ? null : new Point(
            (int) Math.round(bounds.getCenterX()), (int) Math.round(bounds.getCenterY()));
    }

    public boolean containsMouse()
    {
        return contains(Mouse.getPosition());
    }

    public boolean contains(Point point)
    {
        if (point == null) return false;
        Shape shape = getDestinationShape();
        Rectangle limit = containmentBounds;
        return shape != null && shape.contains(point.x + 0.5, point.y + 0.5)
            && (limit == null || limit.contains(point));
    }

    public boolean contains(AbstractMouseDestination<?> other)
    {
        return other != null && contains(other.getCenterPoint());
    }

    public Point getSuitablePoint()
    {
        Shape shape = getDestinationShape();
        Rectangle bounds = getBoundingBox();
        if (shape == null || bounds == null || bounds.isEmpty()) return null;
        Rectangle limit = containmentBounds;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        double deviationX = Math.max(1.0, bounds.width / 5.5);
        double deviationY = Math.max(1.0, bounds.height / 5.5);
        for (int attempt = 0; attempt < 64; attempt++)
        {
            int x = (int) Math.round(centerX + random.nextGaussian() * deviationX);
            int y = (int) Math.round(centerY + random.nextGaussian() * deviationY);
            Point candidate = new Point(x, y);
            if (shape.contains(x + 0.5, y + 0.5)
                && (limit == null || limit.contains(candidate))) return candidate;
        }
        Point center = getCenterPoint();
        return contains(center) ? center : null;
    }

    public boolean canInteract() { return valid() && isVisible(); }
    public boolean canInteract(String action) { return canInteract(); }
    public boolean verifyPostInteract() { return true; }
    public Condition getInterrupt() { return interrupt; }
    public void setInterrupt(Condition interrupt) { this.interrupt = interrupt; }

    @Override
    public String toString()
    {
        return getClass().getSimpleName() + '{' + target + '}';
    }
}
