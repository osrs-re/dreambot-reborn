package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import net.runelite.api.events.HitsplatApplied;

public interface HitSplatListener extends EventListener
{
    default void onHitSplatAdded(Entity entity, int type, int value, int endCycle,
                                 int secondaryType, int secondaryValue) { }

    /** RuneLite-native callback retained for scripts that need the original event. */
    default void onHitSplatAdded(HitsplatApplied event) { }
}
