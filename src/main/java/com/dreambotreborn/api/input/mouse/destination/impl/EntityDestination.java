package com.dreambotreborn.api.input.mouse.destination.impl;

import java.awt.Shape;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.methods.input.Camera;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.utilities.impl.Condition;

public class EntityDestination extends AbstractMouseDestination<Entity>
{
    public EntityDestination(Entity entity)
    {
        super(entity);
    }

    @Override
    public Shape getDestinationShape()
    {
        return getTarget() == null ? null : getTarget().clickShape(null);
    }

    @Override public boolean isVisible() { return getDestinationShape() != null; }
    @Override public int type() { return 3; }
    @Override public boolean canInteract(String action)
    {
        return canInteract() && getTarget().hasAction(action);
    }
    @Override
    public boolean handleWalk()
    {
        if (getTarget() == null) return false;
        Walking.walk(getTarget());
        return true;
    }

    @Override
    public boolean handleCamera()
    {
        return Camera.rotateToEntity(getTarget());
    }

    @Override
    public boolean handleCamera(boolean async, Condition condition)
    {
        boolean rotated = handleCamera();
        return rotated && (condition == null || async || condition.verify());
    }

    @Override
    public boolean verifyPostInteract()
    {
        return getTarget() != null;
    }
}
