package com.dreambotreborn.api.wrappers.interactive;

import java.awt.Component;
import java.awt.Shape;
import net.runelite.api.Actor;
import com.dreambotreborn.api.DreamBotRebornApi;

/** Shared snapshot data for players and NPCs. */
public abstract class Character extends Entity
{
    public final int combatLevel;
    public final int animation;
    public final int orientation;
    public final int healthPercent;
    public final boolean interacting;

    private final Actor actor;

    Character(int id, String name, String[] actions, Actor actor)
    {
        super(id, name, actions, actor.getWorldLocation(), actor.getLocalLocation());
        this.actor = actor;
        this.combatLevel = actor.getCombatLevel();
        this.animation = actor.getAnimation();
        this.orientation = actor.getOrientation();
        this.healthPercent = healthPercent(actor.getHealthRatio(), actor.getHealthScale());
        this.interacting = actor.isInteracting();
    }

    public final int getCombatLevel()
    {
        return combatLevel;
    }

    public final int getAnimation()
    {
        return animation;
    }

    public final boolean isAnimating()
    {
        return animation >= 0;
    }

    public final boolean isMoving()
    {
        return actor.getPoseAnimation() != actor.getIdlePoseAnimation();
    }

    public final boolean isHealthBarVisible()
    {
        return actor.getHealthRatio() >= 0 && actor.getHealthScale() > 0;
    }

    public final int getSpotAnimation()
    {
        return actor.getGraphic();
    }

    public final boolean isInteracting(Character target)
    {
        return target != null && actor.getInteracting() == target.unwrapActor();
    }

    public final int getOrientation()
    {
        return orientation;
    }

    public final int getHealthPercent()
    {
        return healthPercent;
    }

    public final boolean isInteracting()
    {
        return interacting;
    }

    public final Actor getInteracting()
    {
        return actor.getInteracting();
    }

    public final boolean isOnScreen()
    {
        Shape hull = actor.getConvexHull();
        return hull != null && hull.getBounds().intersects(
            0, 0, DreamBotRebornApi.requireClient().getCanvasWidth(), DreamBotRebornApi.requireClient().getCanvasHeight());
    }

    @Override
    public final Shape clickShape(Component component)
    {
        Shape hull = actor.getConvexHull();
        return hull != null ? hull : actor.getCanvasTilePoly();
    }

    public final Actor unwrapActor()
    {
        return actor;
    }

    static int healthPercent(int ratio, int scale)
    {
        if (ratio < 0 || scale <= 0)
        {
            return -1;
        }
        return Math.max(0, Math.min(100, (int) Math.round(ratio * 100.0 / scale)));
    }
}
