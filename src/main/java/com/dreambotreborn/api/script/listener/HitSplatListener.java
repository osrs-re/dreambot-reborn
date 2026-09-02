package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.HitsplatApplied;

public interface HitSplatListener extends EventListener
{
    default void onHitSplatAdded(HitsplatApplied event) { }
}
