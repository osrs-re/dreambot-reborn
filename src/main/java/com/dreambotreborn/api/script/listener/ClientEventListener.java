package com.dreambotreborn.api.script.listener;

import java.util.EventListener;

/** Catch-all listener for raw RuneLite events not covered by a narrower listener. */
public interface ClientEventListener extends EventListener
{
    default void onClientEvent(Object event) { }
}
