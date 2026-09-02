package com.dreambotreborn.api.methods.skills;

/** Skills exposed with DreamBot-compatible names. */
public enum Skill
{
    ATTACK(net.runelite.api.Skill.ATTACK),
    DEFENCE(net.runelite.api.Skill.DEFENCE),
    STRENGTH(net.runelite.api.Skill.STRENGTH),
    HITPOINTS(net.runelite.api.Skill.HITPOINTS),
    RANGED(net.runelite.api.Skill.RANGED),
    PRAYER(net.runelite.api.Skill.PRAYER),
    MAGIC(net.runelite.api.Skill.MAGIC),
    COOKING(net.runelite.api.Skill.COOKING),
    WOODCUTTING(net.runelite.api.Skill.WOODCUTTING),
    FLETCHING(net.runelite.api.Skill.FLETCHING),
    FISHING(net.runelite.api.Skill.FISHING),
    FIREMAKING(net.runelite.api.Skill.FIREMAKING),
    CRAFTING(net.runelite.api.Skill.CRAFTING),
    SMITHING(net.runelite.api.Skill.SMITHING),
    MINING(net.runelite.api.Skill.MINING),
    HERBLORE(net.runelite.api.Skill.HERBLORE),
    AGILITY(net.runelite.api.Skill.AGILITY),
    THIEVING(net.runelite.api.Skill.THIEVING),
    SLAYER(net.runelite.api.Skill.SLAYER),
    FARMING(net.runelite.api.Skill.FARMING),
    RUNECRAFTING(net.runelite.api.Skill.RUNECRAFT),
    HUNTER(net.runelite.api.Skill.HUNTER),
    CONSTRUCTION(net.runelite.api.Skill.CONSTRUCTION),
    SAILING(net.runelite.api.Skill.SAILING);

    private final net.runelite.api.Skill runeLiteSkill;

    Skill(net.runelite.api.Skill runeLiteSkill)
    {
        this.runeLiteSkill = runeLiteSkill;
    }

    net.runelite.api.Skill unwrap()
    {
        return runeLiteSkill;
    }

    public int getId()
    {
        return ordinal();
    }

    public String getName()
    {
        return runeLiteSkill.getName();
    }

    public int getBoostedLevel()
    {
        return Skills.getBoostedLevel(this);
    }

    public int getLevel()
    {
        return Skills.getRealLevel(this);
    }

    public int getExperience()
    {
        return Skills.getExperience(this);
    }

    public int getExperienceToLevel()
    {
        return Skills.getExperienceToLevel(this);
    }

    public static Skill forId(int id)
    {
        return id < 0 || id >= values().length ? null : values()[id];
    }

    @Override
    public String toString()
    {
        return getName();
    }
}
