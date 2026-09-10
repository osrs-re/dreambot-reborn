package com.dreambotreborn.api.methods.quest.book.requirement;

import java.util.Arrays;
import com.dreambotreborn.api.methods.quest.book.Quest;

public class QuestRequirement extends Requirement
{
    private Quest[] requires;
    private final int minimumCompleted;

    public QuestRequirement(int minimumCompleted, Quest... requires)
    {
        this.minimumCompleted = Math.max(0, minimumCompleted);
        setRequires(requires);
    }
    public QuestRequirement(Quest... requires)
    {
        this(requires == null ? 0 : requires.length, requires);
    }
    @Override public boolean meetsRequirement()
    {
        int completed = 0;
        for (Quest quest : requires) if (quest != null && quest.isFinished()) completed++;
        return completed >= minimumCompleted;
    }
    public Quest[] getRequires() { return Arrays.copyOf(requires, requires.length); }
    public void setRequires(Quest[] value)
    {
        requires = value == null ? new Quest[0] : Arrays.copyOf(value, value.length);
    }
}
