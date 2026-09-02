package com.dreambotreborn.api.input.mouse.destination.impl;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;

public class PointDestination extends AbstractMouseDestination<Point>
{
    public PointDestination(Point point)
    {
        super(point == null ? null : new Point(point));
    }

    @Override
    public Shape getDestinationShape()
    {
        Point point = getTarget();
        return point == null ? null : new Rectangle(point.x - 1, point.y - 1, 3, 3);
    }

    @Override public boolean isVisible() { return getTarget() != null; }
    @Override public int type() { return 1; }
    @Override public Point getSuitablePoint() { return getTarget() == null ? null : new Point(getTarget()); }
}
