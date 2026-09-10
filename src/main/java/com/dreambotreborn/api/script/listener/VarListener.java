package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.VarbitChanged;

public interface VarListener extends EventListener
{
    default void onVarBitUpdate(int id, int previous, int current) { }
    default void onVarBitUpdate(int id, int current) { }
    default void onVarpUpdate(int id, int previous, int current) { }
    default void onVarpUpdate(int id, int current) { }

    /** RuneLite-native callback retained for lower-level integrations. */
    default void onVarBitUpdate(VarbitChanged event) { }
}
