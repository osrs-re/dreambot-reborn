package com.dreambotreborn.api.methods.quest.book;

import java.util.ArrayList;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.quest.Quests;
import com.dreambotreborn.api.methods.quest.book.requirement.Requirement;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public interface Quest
{
    enum State
    {
        FINISHED, STARTED, NOT_STARTED, INVALID;

        public static State getForID(int id)
        {
            switch (id)
            {
                case 0: return NOT_STARTED;
                case 1: return STARTED;
                case 2: return FINISHED;
                default: return INVALID;
            }
        }
    }

    enum Type { F2P, P2P, MINIQUEST }

    static Quest[] values()
    {
        List<Quest> quests = new ArrayList<>();
        java.util.Collections.addAll(quests, FreeQuest.values());
        java.util.Collections.addAll(quests, PaidQuest.values());
        java.util.Collections.addAll(quests, MiniQuest.values());
        return quests.toArray(new Quest[0]);
    }

    default Type getType()
    {
        return this instanceof FreeQuest ? Type.F2P
            : this instanceof MiniQuest ? Type.MINIQUEST : Type.P2P;
    }

    default int getParent() { return 399; }
    int getQP();
    default int getChild() { return -1; }
    default int getGrandChild() { return -1; }
    default int[] getSettings() { return new int[0]; }
    default int getStartedSetting() { return 1; }
    default int getFinishedSetting() { return 2; }
    default int getConfigId()
    {
        net.runelite.api.Quest quest = Quests.toRuneLite(this);
        return quest == null ? -1 : quest.getId();
    }
    default int getConfigID() { return getConfigId(); }
    default int getVarBitId() { return -1; }
    default int getVarBitID() { return getVarBitId(); }
    default Requirement getQuests() { return null; }
    default Requirement getSkills() { return null; }
    default int getConfigValue()
    {
        int id = getConfigId();
        if (id < 0) return -1;
        try { return DreamBotRebornApi.requireClient().getVarpValue(id); }
        catch (RuntimeException ignored) { return -1; }
    }
    default boolean hasRequirements()
    {
        return getQuests() != null || getSkills() != null;
    }
    default int getColor()
    {
        switch (getState())
        {
            case FINISHED: return 0x00ff00;
            case STARTED: return 0xffff00;
            case NOT_STARTED: return 0xff0000;
            default: return 0xffffff;
        }
    }
    default boolean find() { return getWidgetChild() != null; }
    default WidgetChild getWidgetChild()
    {
        String name = Quests.getDisplayName(this);
        return com.dreambotreborn.api.methods.widget.Widgets.getMatchingWidget(widget ->
            widget.visible && widget.text.equalsIgnoreCase(name));
    }
    default State getState() { return Quests.getState(this); }
    default boolean isStarted() { return getState() == State.STARTED; }
    default boolean isFinished() { return getState() == State.FINISHED; }
}
