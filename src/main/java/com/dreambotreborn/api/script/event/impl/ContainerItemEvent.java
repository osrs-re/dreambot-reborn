package com.dreambotreborn.api.script.event.impl;

import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.wrappers.items.Item;

abstract class ContainerItemEvent extends ScriptEvent
{
    final Item previous;
    final Item current;

    ContainerItemEvent(Item current) { this(null, current); }
    ContainerItemEvent(Item previous, Item current)
    { this.previous = previous; this.current = current; }

    final boolean added() { return previous == null && current != null; }
    final boolean removed() { return previous != null && current == null; }
    final boolean swapped()
    {
        return previous != null && current != null && previous.getSlot() != current.getSlot();
    }

    @Override public String toString()
    {
        return getClass().getSimpleName() + "{" + previous + " -> " + current + '}';
    }
}
