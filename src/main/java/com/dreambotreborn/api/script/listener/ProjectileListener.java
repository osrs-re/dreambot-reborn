package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.ProjectileMoved;

public interface ProjectileListener extends EventListener
{
    default void onProjectileMoved(ProjectileMoved event) { }
}
