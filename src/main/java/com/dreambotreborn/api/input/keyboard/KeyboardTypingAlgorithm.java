package com.dreambotreborn.api.input.keyboard;

/** Pluggable timing and mistake policy for the virtual keyboard. */
public interface KeyboardTypingAlgorithm
{
    int delayBefore(char previous, char current);

    int keyHoldMillis(char value);

    default Character mistakeFor(char intended)
    {
        return null;
    }
}
