package com.dreambotreborn.api.script.frameworks.utility;

import com.dreambotreborn.api.utilities.Sleep;
import com.dreambotreborn.api.utilities.impl.Condition;

/** DreamBot-compatible sleep mixin used by scripts and task nodes. */
public interface Sleepable
{
    default void sleep(long milliseconds)
    {
        Sleep.sleep(milliseconds);
    }

    default void sleep(long minimum, long maximum)
    {
        Sleep.sleep(minimum, maximum);
    }

    default boolean sleepUntil(Condition condition, Condition reset, long timeout, long poll)
    {
        return Sleep.sleepUntil(condition, reset, timeout, poll);
    }

    default boolean sleepUntil(Condition condition, long timeout, long poll)
    {
        return Sleep.sleepUntil(condition, timeout, poll);
    }

    default boolean sleepWhile(Condition condition, Condition reset, long timeout, long poll)
    {
        return Sleep.sleepWhile(condition, reset, timeout, poll);
    }

    default boolean sleepWhile(Condition condition, long timeout, long poll)
    {
        return Sleep.sleepWhile(condition, timeout, poll);
    }
}
