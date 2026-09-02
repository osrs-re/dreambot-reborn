package com.dreambotreborn.api.randoms;

import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.wrappers.interactive.NPC;

/** Dismisses a nearby optional random-event NPC when one exposes that action. */
public class DismissSolver extends RandomSolver
{
    public DismissSolver()
    {
        super(RandomEvent.DISMISS);
        setMinimumRest(1_500);
    }

    @Override
    public boolean shouldExecute()
    {
        return System.currentTimeMillis() - lastRan() >= getMinimumRest()
            && NPCs.closest(npc -> npc.hasAction("Dismiss")) != null;
    }

    @Override
    public int onLoop()
    {
        NPC npc = NPCs.closest(candidate -> candidate.hasAction("Dismiss"));
        if (npc != null) npc.interact("Dismiss");
        return 800;
    }
}
