package com.dreambotreborn.api.input.mouse.destination.impl.shape;

public final class AreaDestination extends ShapeDestination<java.awt.geom.Area>
{
    public AreaDestination(java.awt.geom.Area area)
    {
        super(area == null ? null : new java.awt.geom.Area(area));
    }
}
