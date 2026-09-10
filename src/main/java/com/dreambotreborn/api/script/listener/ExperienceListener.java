package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.impl.ExperienceEvent;

public interface ExperienceListener extends EventListener
{
    default void onGained(ExperienceEvent event) { }
    default void onLevelUp(ExperienceEvent event) { }
    default void onLevelChange(ExperienceEvent event) { }
    default void onClientEvent(ExperienceEvent event) { }
}
