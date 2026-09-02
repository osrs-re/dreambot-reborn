package com.dreambotreborn.api.methods.filter.impl;

import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.wrappers.interactive.Interactable;

public final class ActionFilter<E extends Interactable> implements Filter<E>
{
    private final String[] actions;

    public ActionFilter(String... actions)
    {
        this.actions = actions == null ? new String[0] : actions.clone();
    }

    @Override
    public boolean match(E value)
    {
        if (value == null) return false;
        for (String action : actions) if (value.hasAction(action)) return true;
        return false;
    }
}
