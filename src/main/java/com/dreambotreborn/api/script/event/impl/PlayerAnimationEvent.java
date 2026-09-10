package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.AnimationListener;
import com.dreambotreborn.api.wrappers.interactive.Player;

public class PlayerAnimationEvent extends ScriptEvent
{
    private final Player player;
    private final int previous;
    private final int current;
    public PlayerAnimationEvent(int index, int previous, int current)
    { this(Players.getAtIndex(index), previous, current); }
    public PlayerAnimationEvent(Object player, int previous, int current)
    { this(player instanceof Player ? (Player) player : null, previous, current); }
    public PlayerAnimationEvent(Player player, int previous, int current)
    { this.player = player; this.previous = previous; this.current = current; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof AnimationListener && player != null)
            ((AnimationListener) listener).onPlayerAnimation(player, previous, current);
    }
    @Override public String toString()
    { return "PlayerAnimationEvent{" + player + ", " + previous + " -> " + current + '}'; }
}
