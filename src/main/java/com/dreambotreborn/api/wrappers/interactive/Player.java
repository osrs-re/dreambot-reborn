package com.dreambotreborn.api.wrappers.interactive;

import net.runelite.api.MenuAction;
import net.runelite.api.WorldView;
import com.dreambotreborn.api.DreamBotRebornApi;

/** Immutable player snapshot. */
public final class Player extends Character
{
    public final int index;
    public final int team;
    public final boolean local;

    private final net.runelite.api.Player player;

    public Player(net.runelite.api.Player player, String[] actions, boolean local)
    {
        super(player.getId(), player.getName(), actions, player);
        this.player = player;
        this.index = player.getId();
        this.team = player.getTeam();
        this.local = local;
    }

    public int getIndex()
    {
        return index;
    }

    public int getTeam()
    {
        return team;
    }

    public boolean isLocal()
    {
        return local;
    }

    @Override
    public InteractionSpec interactionAt(int actionIndex)
    {
        MenuAction[] menuActions =
        {
            MenuAction.PLAYER_FIRST_OPTION,
            MenuAction.PLAYER_SECOND_OPTION,
            MenuAction.PLAYER_THIRD_OPTION,
            MenuAction.PLAYER_FOURTH_OPTION,
            MenuAction.PLAYER_FIFTH_OPTION,
            MenuAction.PLAYER_SIXTH_OPTION,
            MenuAction.PLAYER_SEVENTH_OPTION,
            MenuAction.PLAYER_EIGHTH_OPTION
        };
        if (local || actionIndex < 0 || actionIndex >= menuActions.length)
        {
            return null;
        }
        WorldView view = player.getWorldView();
        return new InteractionSpec(
            0,
            0,
            DreamBotRebornApi.requireClient().isWidgetSelected()
                ? MenuAction.ITEM_USE_ON_PLAYER : menuActions[actionIndex],
            index,
            actionAt(actionIndex),
            name,
            view == null ? WorldView.TOPLEVEL : view.getId());
    }

    public net.runelite.api.Player unwrap()
    {
        return player;
    }

    @Override
    public String toString()
    {
        return "Player{" + "name='" + name + '\'' + ", combatLevel=" + combatLevel +
            ", worldLocation=" + worldLocation + '}';
    }
}
