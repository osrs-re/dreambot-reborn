package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.methods.cs2.RuneScriptEvent;

public interface RSScriptEventListener extends EventListener
{
    default void preFired(RuneScriptEvent event) { }
    default void onPreFired(RuneScriptEvent event) { }
}
