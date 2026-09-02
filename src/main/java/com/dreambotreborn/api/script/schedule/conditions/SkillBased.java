package com.dreambotreborn.api.script.schedule.conditions;

import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.methods.skills.Skills;

public class SkillBased implements StopCondition
{
    private Skill skill = Skill.ATTACK;
    private int level = 99;

    @Override
    public boolean shouldStop()
    {
        return Skills.getRealLevel(skill) >= level;
    }

    public Skill getSkill() { return skill; }
    public int getLevel() { return level; }
    public void setSkill(Skill skill) { this.skill = skill == null ? Skill.ATTACK : skill; }
    public void setLevel(int level) { this.level = Math.max(1, Math.min(126, level)); }
}
