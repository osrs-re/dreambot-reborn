package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.impl.ScriptPrefiredEvent;

public interface RuneScriptListener extends EventListener
{
    default void onPrefired(ScriptPrefiredEvent event) { }
}
