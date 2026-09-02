package com.dreambotreborn.api.input.mouse.destination.impl.shape;

import java.awt.Rectangle;

public final class RectangleDestination extends ShapeDestination<Rectangle>
{
    public RectangleDestination(Rectangle rectangle)
    {
        super(rectangle == null ? null : new Rectangle(rectangle));
    }
}
