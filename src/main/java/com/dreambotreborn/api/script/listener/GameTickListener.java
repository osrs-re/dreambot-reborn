package com.dreambotreborn.api.script.listener;

import java.util.EventListener;

public interface GameTickListener extends EventListener
{
    default void onGameTick() { }
    default void onPreTick() { }
    default void onServerTick() { }
}
