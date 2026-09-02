package com.dreambotreborn.api.input.mouse.destination.impl.shape;

import java.awt.Shape;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;

public class ShapeDestination<T extends Shape> extends AbstractMouseDestination<T>
{
    public ShapeDestination(T shape)
    {
        super(shape);
    }

    @Override public T getDestinationShape() { return getTarget(); }
    @Override public boolean isVisible() { return getTarget() != null && !getTarget().getBounds().isEmpty(); }
    @Override public int type() { return 2; }
}
