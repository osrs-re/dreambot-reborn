package com.dreambotreborn.api.wrappers.interactive;

import net.runelite.api.MenuAction;
import net.runelite.api.NPCComposition;
import net.runelite.api.WorldView;
import com.dreambotreborn.api.DreamBotRebornApi;

/** Immutable NPC snapshot. */
public final class NPC extends Character
{
    public final int index;
    public final int size;

    private final net.runelite.api.NPC npc;

    public NPC(net.runelite.api.NPC npc, NPCComposition composition)
    {
        super(
            npc.getId(),
            composition == null ? npc.getName() : composition.getName(),
            composition == null ? null : composition.getActions(),
            npc);
        this.npc = npc;
        this.index = npc.getIndex();
        this.size = composition == null ? 1 : composition.getSize();
    }

    public int getIndex()
    {
        return index;
    }

    public int getSize()
    {
        return size;
    }

    @Override
    public InteractionSpec interactionAt(int actionIndex)
    {
        MenuAction[] menuActions =
        {
            MenuAction.NPC_FIRST_OPTION,
            MenuAction.NPC_SECOND_OPTION,
            MenuAction.NPC_THIRD_OPTION,
            MenuAction.NPC_FOURTH_OPTION,
            MenuAction.NPC_FIFTH_OPTION
        };
        if (actionIndex < 0 || actionIndex >= menuActions.length)
        {
            return null;
        }
        WorldView view = npc.getWorldView();
        return new InteractionSpec(
            0,
            0,
            DreamBotRebornApi.requireClient().isWidgetSelected()
                ? MenuAction.WIDGET_TARGET_ON_NPC : menuActions[actionIndex],
            index,
            actionAt(actionIndex),
            name,
            view == null ? WorldView.TOPLEVEL : view.getId());
    }

    public net.runelite.api.NPC unwrap()
    {
        return npc;
    }

    @Override
    public String toString()
    {
        return "NPC{" + "id=" + id + ", name='" + name + '\'' +
            ", combatLevel=" + combatLevel + ", worldLocation=" + worldLocation + '}';
    }
}
