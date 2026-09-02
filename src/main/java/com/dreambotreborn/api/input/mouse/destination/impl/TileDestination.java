package com.dreambotreborn.api.input.mouse.destination.impl;

import java.awt.Shape;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;
import com.dreambotreborn.api.methods.map.Tile;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import com.dreambotreborn.api.methods.input.Camera;
import com.dreambotreborn.api.utilities.impl.Condition;

public class TileDestination extends AbstractMouseDestination<Tile>
{
    public TileDestination(Tile tile)
    {
        super(tile);
    }

    @Override
    public Shape getDestinationShape()
    {
        if (getTarget() == null) return null;
        LocalPoint local = LocalPoint.fromWorld(DreamBotRebornApi.requireClient(), getTarget().toWorldPoint());
        return local == null ? null : Perspective.getCanvasTilePoly(DreamBotRebornApi.requireClient(), local);
    }

    @Override public boolean isVisible() { return getDestinationShape() != null; }
    @Override public int type() { return 4; }
    @Override public boolean handleCamera(boolean async, Condition condition)
    {
        boolean rotated = Camera.rotateToTile(getTarget());
        return rotated && (condition == null || async || condition.verify());
    }
    @Override public boolean verifyPostInteract() { return getTarget() != null; }
}
