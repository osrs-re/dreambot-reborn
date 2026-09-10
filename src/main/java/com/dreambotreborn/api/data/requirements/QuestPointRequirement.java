package com.dreambotreborn.api.data.requirements;

import com.dreambotreborn.api.methods.quest.Quests;

public class QuestPointRequirement extends Requirement
{
    private int points;
    public QuestPointRequirement(int points) { this.points = points; }
    @Override public boolean meetsRequirement() { return Quests.getQuestPoints() >= points; }
    public int getPoints() { return points; }
    public void setPoints(int value) { points = value; }
}
