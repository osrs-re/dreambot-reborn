package com.dreambotreborn.api.methods.input.mouse;

/** Supplies an actual press-to-release duration for a virtual click. */
@FunctionalInterface
public interface MouseTiming
{
    int getClickTimingInMilliseconds();
}
