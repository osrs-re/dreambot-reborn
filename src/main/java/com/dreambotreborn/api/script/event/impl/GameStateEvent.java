package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.data.GameState;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.GameStateListener;

public class GameStateEvent extends ScriptEvent
{
    private final GameState state;
    public GameStateEvent(GameState state) { this.state = state == null ? GameState.NULL : state; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof GameStateListener)
            ((GameStateListener) listener).onGameStateChange(state);
    }
}
