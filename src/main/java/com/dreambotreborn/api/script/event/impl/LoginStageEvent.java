package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.LoginListener;

public class LoginStageEvent extends ScriptEvent
{
    private final int stage;
    public LoginStageEvent(int stage) { this.stage = stage; }
    @Override public void dispatch(EventListener listener)
    { if (listener instanceof LoginListener) ((LoginListener) listener).onLoginStageChange(stage); }
}
