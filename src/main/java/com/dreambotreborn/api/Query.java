package com.dreambotreborn.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Stream;
import com.dreambotreborn.api.wrappers.interactive.Entity;

/** An immutable snapshot of query results. */
public final class Query<T> implements Iterable<T>
{
    private final List<T> results;

    public Query(List<T> results)
    {
        this.results = Collections.unmodifiableList(new ArrayList<>(results));
    }

    /**
     * Returns the first result, or {@code null} when the query is empty.
     */
    public T first()
    {
        return results.isEmpty() ? null : results.get(0);
    }

    public Optional<T> firstOptional()
    {
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Query<T> filter(Predicate<? super T> predicate)
    {
        List<T> filtered = new ArrayList<>();
        for (T result : results)
        {
            if (predicate.test(result))
            {
                filtered.add(result);
            }
        }
        return new Query<>(filtered);
    }

    public T first(Predicate<? super T> predicate)
    {
        return filter(predicate).first();
    }

    public T last()
    {
        return results.isEmpty() ? null : results.get(results.size() - 1);
    }

    public T random()
    {
        return results.isEmpty() ? null
            : results.get(ThreadLocalRandom.current().nextInt(results.size()));
    }

    /** Keeps entities within the supplied tile distance of the local player. */
    public Query<T> within(int distance)
    {
        if (distance < 0)
        {
            throw new IllegalArgumentException("distance cannot be negative");
        }
        return filter(value -> value instanceof Entity
            && ((Entity) value).distance() <= distance);
    }

    /** Returns the nearest entity, or the first result for non-entity queries. */
    public T nearest()
    {
        T nearest = null;
        int distance = Integer.MAX_VALUE;
        for (T value : results)
        {
            if (!(value instanceof Entity))
            {
                return first();
            }
            int candidate = ((Entity) value).distance();
            if (nearest == null || candidate < distance)
            {
                nearest = value;
                distance = candidate;
            }
        }
        return nearest;
    }

    public Query<T> sorted(Comparator<? super T> comparator)
    {
        List<T> sorted = new ArrayList<>(results);
        sorted.sort(Objects.requireNonNull(comparator, "comparator"));
        return new Query<>(sorted);
    }

    public boolean exists()
    {
        return !results.isEmpty();
    }

    public List<T> all()
    {
        return results;
    }

    public int count()
    {
        return results.size();
    }

    public boolean isEmpty()
    {
        return results.isEmpty();
    }

    public Stream<T> stream()
    {
        return results.stream();
    }

    @Override
    public Iterator<T> iterator()
    {
        return results.iterator();
    }
}
