package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.ExperienceListener;

public class ExperienceEvent extends ScriptEvent
{
    public static final int GAINED = 0;
    public static final int LEVEL_UP = 1;
    public static final int LEVEL_CHANGE = 2;
    private final int type;
    private final int change;
    private final Skill skill;

    public ExperienceEvent(int type, int change, Skill skill)
    {
        this.type = type;
        this.change = change;
        this.skill = skill;
    }
    public int getType() { return type; }
    public int getChange() { return change; }
    public Skill getSkill() { return skill; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof ExperienceListener)) return;
        ExperienceListener target = (ExperienceListener) listener;
        target.onClientEvent(this);
        if (type == LEVEL_UP) target.onLevelUp(this);
        else if (type == LEVEL_CHANGE) target.onLevelChange(this);
        else target.onGained(this);
    }
    @Override public String toString() { return "ExperienceEvent{" + skill + ", change=" + change + '}'; }
}
