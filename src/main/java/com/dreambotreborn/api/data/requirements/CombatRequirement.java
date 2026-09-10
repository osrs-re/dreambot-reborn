package com.dreambotreborn.api.data.requirements;

import com.dreambotreborn.api.methods.combat.Combat;

public class CombatRequirement extends Requirement
{
    private int level;
    public CombatRequirement(int level) { this.level = level; }
    @Override public boolean meetsRequirement() { return Combat.getCombatLevel() >= level; }
    public int getLevel() { return level; }
    public void setLevel(int value) { level = value; }
}
