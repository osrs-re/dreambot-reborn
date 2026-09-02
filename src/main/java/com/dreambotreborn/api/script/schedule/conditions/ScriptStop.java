package com.dreambotreborn.api.script.schedule.conditions;

import com.dreambotreborn.api.script.ScriptManager;

public class ScriptStop implements StopCondition
{
    @Override public boolean shouldStop() { return !ScriptManager.getScriptManager().isRunning(); }
}
