package com.dreambotreborn.api.data.requirements;

import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.methods.skills.Skills;

public class SkillRequirement extends Requirement
{
    private Skill skill;
    private int level;
    private boolean boosted;
    public SkillRequirement(Skill skill, int level) { this(skill, level, false); }
    public SkillRequirement(Skill skill, int level, boolean boosted)
    { this.skill = skill; this.level = level; this.boosted = boosted; }
    @Override public boolean meetsRequirement()
    { return skill != null && (boosted ? Skills.getBoostedLevel(skill) : Skills.getRealLevel(skill)) >= level; }
    public Skill getSkill() { return skill; }
    public int getLevel() { return level; }
    public boolean isBoosted() { return boosted; }
    public void setSkill(Skill value) { skill = value; }
    public void setLevel(int value) { level = value; }
    public void setBoosted(boolean value) { boosted = value; }
}
