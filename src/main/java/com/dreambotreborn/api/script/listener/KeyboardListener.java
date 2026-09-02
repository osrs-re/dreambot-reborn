package com.dreambotreborn.api.script.listener;

import java.awt.event.KeyEvent;
import java.util.EventListener;

public interface KeyboardListener extends EventListener
{
    default void onKeyPressed(KeyEvent event) { }
    default void onKeyReleased(KeyEvent event) { }
    default void onKeyTyped(KeyEvent event) { }
}
