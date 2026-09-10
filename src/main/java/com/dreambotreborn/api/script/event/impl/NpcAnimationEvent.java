package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.AnimationListener;
import com.dreambotreborn.api.wrappers.interactive.NPC;

public class NpcAnimationEvent extends ScriptEvent
{
    private final NPC npc;
    private final int previous;
    private final int current;
    public NpcAnimationEvent(NPC npc, int index, int previous, int current)
    { this.npc = npc; this.previous = previous; this.current = current; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof AnimationListener && npc != null)
            ((AnimationListener) listener).onNpcAnimation(npc, previous, current);
    }
    @Override public String toString()
    { return "NpcAnimationEvent{" + npc + ", " + previous + " -> " + current + '}'; }
}
