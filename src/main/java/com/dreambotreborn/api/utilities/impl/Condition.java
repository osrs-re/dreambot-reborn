package com.dreambotreborn.api.utilities.impl;

/** Boolean condition used by the script sleep helpers. */
@FunctionalInterface
public interface Condition
{
    boolean verify();

    default Condition and(Condition other)
    {
        return () -> verify() && other.verify();
    }

    default Condition or(Condition other)
    {
        return () -> verify() || other.verify();
    }

    default Condition not(Condition other)
    {
        return () -> verify() && !other.verify();
    }

    default Condition negate()
    {
        return () -> !verify();
    }
}
