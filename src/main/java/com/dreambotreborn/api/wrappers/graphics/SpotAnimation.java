package com.dreambotreborn.api.wrappers.graphics;

/** Immutable spot/graphic-animation snapshot used by script animation events. */
public class SpotAnimation
{
    private final int animationId;
    private final int delay;
    private final int tick;

    public SpotAnimation(int animationId, int delay, int tick)
    {
        this.animationId = animationId;
        this.delay = delay;
        this.tick = tick;
    }

    public int getDelay()
    {
        return delay;
    }

    public int getAnimationId()
    {
        return animationId;
    }

    public int getTick()
    {
        return tick;
    }

    @Override
    public String toString()
    {
        return "SpotAnimation{" + animationId + ", delay=" + delay + ", tick=" + tick + '}';
    }
}
