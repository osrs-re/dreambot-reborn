package com.dreambotreborn.api.methods.input.mouse;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

/** Bounded coordinate history used by diagnostics and script paint. */
public final class MouseTracker
{
    private final Object lock = new Object();
    private final Deque<Point> coordinates = new ArrayDeque<>();
    private final int capacity;
    private volatile boolean tracking = true;

    public MouseTracker()
    {
        this(2048);
    }

    public MouseTracker(int capacity)
    {
        if (capacity <= 0)
        {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public boolean getTracking()
    {
        return tracking;
    }

    public void setTracking(boolean tracking)
    {
        this.tracking = tracking;
    }

    public Object getLock()
    {
        return lock;
    }

    public int getLength()
    {
        synchronized (lock)
        {
            return coordinates.size();
        }
    }

    public int[] getXCoordinates()
    {
        synchronized (lock)
        {
            return coordinates.stream().mapToInt(point -> point.x).toArray();
        }
    }

    public int[] getYCoordinates()
    {
        synchronized (lock)
        {
            return coordinates.stream().mapToInt(point -> point.y).toArray();
        }
    }

    public void record(int x, int y)
    {
        if (!tracking)
        {
            return;
        }
        synchronized (lock)
        {
            coordinates.addLast(new Point(x, y));
            while (coordinates.size() > capacity)
            {
                coordinates.removeFirst();
            }
        }
    }

    public void clear()
    {
        synchronized (lock)
        {
            coordinates.clear();
        }
    }
}
