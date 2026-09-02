package com.dreambotreborn.api.wrappers.interactive.interact;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.utilities.Sleep;
import com.dreambotreborn.api.utilities.impl.Condition;

/**
 * DreamBot-compatible base class for entities which can be targeted by the
 * software mouse. The older DreamBot Reborn interface is retained so existing scripts
 * and the asynchronous interaction engine continue to share one contract.
 */
public abstract class Interactable
    implements com.dreambotreborn.api.wrappers.interactive.Interactable
{
    public Point getCenterPoint()
    {
        Shape shape = mouseTarget().clickShape(null);
        if (shape == null)
        {
            return new Point(-1, -1);
        }
        Rectangle bounds = shape.getBounds();
        return new Point((int) Math.round(bounds.getCenterX()),
            (int) Math.round(bounds.getCenterY()));
    }

    public Point getClickablePoint()
    {
        Shape shape = mouseTarget().clickShape(null);
        Point center = getCenterPoint();
        return shape != null && shape.contains(center) ? center : new Point(-1, -1);
    }

    public boolean interact(Filter<String> filter)
    {
        if (filter == null)
        {
            return false;
        }
        for (String action : getActions())
        {
            if (filter.match(action))
            {
                return interact(action);
            }
        }
        return false;
    }

    public boolean interactForceRight(String action)
    {
        // Menu routing is installed by VirtualMouse, so explicit right-click
        // selection resolves to the same exact action without coordinate races.
        return interact(action);
    }

    public boolean interactForceLeft(String action)
    {
        return interact(action);
    }

    public boolean interact(String action, boolean forceRight, boolean forceLeft)
    {
        return interact(action);
    }

    public boolean interact(
        String action, boolean forceRight, boolean forceLeft, Condition condition)
    {
        if (!interact(action))
        {
            return false;
        }
        return condition == null || Sleep.sleepUntil(condition, 5_000L, 20L);
    }

    public boolean rightClick(Condition condition)
    {
        Point point = getClickablePoint();
        if (point.x < 0 || !Mouse.click(point, true))
        {
            return false;
        }
        return condition == null || Sleep.sleepUntil(condition, 5_000L, 20L);
    }

    public boolean hover(String action, Condition condition)
    {
        if (action != null && !hasAction(action))
        {
            return false;
        }
        Point point = getClickablePoint();
        if (point.x < 0 || !Mouse.move(point))
        {
            return false;
        }
        return condition == null || Sleep.sleepUntil(condition, 5_000L, 20L);
    }

    private MouseTarget mouseTarget()
    {
        if (!(this instanceof MouseTarget))
        {
            throw new IllegalStateException("Interactable is not a mouse target");
        }
        return (MouseTarget) this;
    }
}
