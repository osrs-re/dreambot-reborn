package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.impl.BreakEvent;

public interface BreakListener extends EventListener
{
    default void onBreakStart(BreakEvent event) { }
    default void onBreakEnd(BreakEvent event) { }
}
