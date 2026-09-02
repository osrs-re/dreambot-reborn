package com.dreambotreborn.api.input.mouse.destination.impl.shape;

import java.awt.Polygon;

public final class PolygonDestination extends ShapeDestination<Polygon>
{
    public PolygonDestination(Polygon polygon)
    {
        super(polygon == null ? null
            : new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints));
    }
}
