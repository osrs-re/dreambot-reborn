package com.dreambotreborn.api.methods.quest.book.requirement.skill;

import com.dreambotreborn.api.methods.skills.Skill;

public class SkillReq
{
    private Skill skill;
    private int level;
    public SkillReq(Skill skill, int level) { this.skill = skill; this.level = Math.max(1, level); }
    public int getLevel() { return level; }
    public void setLevel(int value) { level = Math.max(1, value); }
    public Skill getSkill() { return skill; }
    public void setSkill(Skill value) { skill = value; }
}
