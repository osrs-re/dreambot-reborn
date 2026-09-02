package com.dreambotreborn.api.input.mouse.destination.impl;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;
import com.dreambotreborn.api.methods.map.Tile;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;

public class MiniMapTileDestination extends AbstractMouseDestination<Tile>
{
    public MiniMapTileDestination(Tile tile)
    {
        super(tile);
    }

    @Override
    public Shape getDestinationShape()
    {
        Point point = point();
        return point == null ? null : new Rectangle(point.x - 3, point.y - 3, 7, 7);
    }

    @Override public boolean isVisible() { return point() != null; }
    @Override public int type() { return 5; }
    @Override public Point getSuitablePoint() { return point(); }

    private Point point()
    {
        if (getTarget() == null) return null;
        LocalPoint local = LocalPoint.fromWorld(DreamBotRebornApi.requireClient(), getTarget().toWorldPoint());
        net.runelite.api.Point point = local == null ? null
            : Perspective.localToMinimap(DreamBotRebornApi.requireClient(), local);
        return point == null ? null : new Point(point.getX(), point.getY());
    }
}
