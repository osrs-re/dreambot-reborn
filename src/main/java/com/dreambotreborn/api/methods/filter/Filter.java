package com.dreambotreborn.api.methods.filter;

import java.util.Objects;
import java.util.function.Predicate;

/** Source-compatible filter contract that can also be passed to Java Predicate APIs. */
@FunctionalInterface
public interface Filter<T> extends Predicate<T>
{
    boolean match(T value);

    @Override
    default boolean test(T value)
    {
        return match(value);
    }

    default Filter<T> and(Filter<? super T> other)
    {
        Objects.requireNonNull(other, "other");
        return value -> match(value) && other.match(value);
    }

    default Filter<T> or(Filter<? super T> other)
    {
        Objects.requireNonNull(other, "other");
        return value -> match(value) || other.match(value);
    }

    @Override
    default Filter<T> negate()
    {
        return value -> !match(value);
    }
}
