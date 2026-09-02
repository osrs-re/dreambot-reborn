package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.VarbitChanged;

public interface VarListener extends EventListener
{
    default void onVarBitUpdate(VarbitChanged event) { }
}
