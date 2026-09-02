package com.dreambotreborn.api.wrappers.interactive;

/** Common ID/name contract used by DreamBot-style filters. */
public interface Identifiable
{
    int getId();

    default int getID()
    {
        return getId();
    }

    String getName();
}
