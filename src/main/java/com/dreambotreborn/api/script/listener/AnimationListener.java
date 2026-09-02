package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.AnimationChanged;

public interface AnimationListener extends EventListener
{
    default void onAnimationChanged(AnimationChanged event) { }
}
