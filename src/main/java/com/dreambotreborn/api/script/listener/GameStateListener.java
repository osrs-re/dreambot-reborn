package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.GameState;

public interface GameStateListener extends EventListener
{
    default void onGameStateChange(GameState state) { }
}
