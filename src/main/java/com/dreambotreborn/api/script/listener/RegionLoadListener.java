package com.dreambotreborn.api.script.listener;

import java.util.EventListener;

public interface RegionLoadListener extends EventListener
{
    default void onRegionLoad(boolean instanced) { }
}
