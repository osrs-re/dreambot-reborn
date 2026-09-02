package com.dreambotreborn.api.script.schedule.conditions;

public interface StopCondition
{
    boolean shouldStop();
    default void reset() { }
}
