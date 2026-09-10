package com.dreambotreborn.api.data.requirements;

import com.dreambotreborn.api.methods.quest.book.Quest;

public class QuestRequirement extends Requirement
{
    private Quest required;
    public QuestRequirement(Quest required) { this.required = required; }
    @Override public boolean meetsRequirement() { return required != null && required.isFinished(); }
    public Quest getRequired() { return required; }
    public void setRequired(Quest value) { required = value; }
}
