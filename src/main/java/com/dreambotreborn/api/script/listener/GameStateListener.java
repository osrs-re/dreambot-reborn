package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.data.GameState;

public interface GameStateListener extends EventListener
{
    default void onGameStateChange(GameState state) { }

    /** RuneLite-native callback retained for lower-level integrations. */
    default void onGameStateChange(net.runelite.api.GameState state) { }
}
