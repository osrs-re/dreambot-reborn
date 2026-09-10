package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.LoginListener;

public class LoadingStateEvent extends ScriptEvent
{
    private final int state;
    public LoadingStateEvent(int state) { this.state = state; }
    @Override public void dispatch(EventListener listener)
    { if (listener instanceof LoginListener) ((LoginListener) listener).onLoadingStateChange(state); }
}
