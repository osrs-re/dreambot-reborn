package com.dreambotreborn.api.internal;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.wrappers.items.Item;

public final class ContainerQueries
{
    private ContainerQueries()
    {
    }

    public static Query<Item> all(ContainerType type)
    {
        return new Query<>(Containers.items(type));
    }

    public static Query<Item> all(ContainerType type, Predicate<? super Item> predicate)
    {
        return all(type).filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static Item get(ContainerType type, Predicate<? super Item> predicate)
    {
        return all(type, predicate).first();
    }

    public static Item get(ContainerType type, int id)
    {
        return get(type, item -> item.id == id);
    }

    public static Item get(ContainerType type, String name)
    {
        return get(type, item -> name != null && item.name.equalsIgnoreCase(name));
    }

    public static Item getInSlot(ContainerType type, int slot)
    {
        return get(type, item -> item.slot == slot);
    }

    public static boolean contains(ContainerType type, Predicate<? super Item> predicate)
    {
        return get(type, predicate) != null;
    }

    public static int count(ContainerType type, Predicate<? super Item> predicate)
    {
        int count = 0;
        for (Item item : Containers.items(type))
        {
            if (predicate.test(item))
            {
                count += item.amount;
            }
        }
        return count;
    }

    public static int fullSlots(ContainerType type)
    {
        return Containers.items(type).size();
    }

    public static int emptySlots(ContainerType type)
    {
        return Math.max(0, Containers.capacity(type) - fullSlots(type));
    }

    public static int slot(ContainerType type, Predicate<? super Item> predicate)
    {
        Item item = get(type, predicate);
        return item == null ? -1 : item.slot;
    }

    public static boolean onlyContains(ContainerType type, Predicate<? super Item> predicate)
    {
        List<Item> items = Containers.items(type);
        for (Item item : items)
        {
            if (!predicate.test(item))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAllNames(ContainerType type, String... names)
    {
        if (names == null)
        {
            return true;
        }
        for (String name : names)
        {
            if (get(type, name) == null)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAllIds(ContainerType type, int... ids)
    {
        if (ids == null)
        {
            return true;
        }
        for (int id : ids)
        {
            if (get(type, id) == null)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean containsAll(ContainerType type, Collection<?> values)
    {
        if (values == null)
        {
            return true;
        }
        for (Object value : values)
        {
            if (value instanceof Number)
            {
                if (get(type, ((Number) value).intValue()) == null)
                {
                    return false;
                }
            }
            else if (value instanceof String)
            {
                if (get(type, (String) value) == null)
                {
                    return false;
                }
            }
            else if (value instanceof Item)
            {
                if (get(type, ((Item) value).id) == null)
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }
}
