package com.dreambotreborn.api.methods.skills;

import java.util.Arrays;
import net.runelite.api.Experience;
import com.dreambotreborn.api.DreamBotRebornApi;

/** Skill levels and experience captured on the game thread. */
public final class Skills
{
    private static volatile int[] realLevels = new int[Skill.values().length];
    private static volatile int[] boostedLevels = new int[Skill.values().length];
    private static volatile int[] experience = new int[Skill.values().length];
    private static volatile int totalLevel;

    private Skills()
    {
    }

    public static int getRealLevel(Skill skill)
    {
        return skill == null ? 0 : realLevels[skill.ordinal()];
    }

    public static int getBoostedLevel(Skill skill)
    {
        return skill == null ? 0 : boostedLevels[skill.ordinal()];
    }

    public static int getExperience(Skill skill)
    {
        return skill == null ? 0 : experience[skill.ordinal()];
    }

    public static int getExperienceToLevel(Skill skill)
    {
        if (skill == null)
        {
            return 0;
        }
        int level = getRealLevel(skill);
        if (level >= Experience.MAX_REAL_LEVEL)
        {
            return 0;
        }
        return Math.max(0, getExperienceForLevel(level + 1) - getExperience(skill));
    }

    public static int getExperienceForLevel(int level)
    {
        return Experience.getXpForLevel(level);
    }

    public static int getLevelForExperience(int xp)
    {
        return Experience.getLevelForXp(Math.max(0, xp));
    }

    public static int getTotalLevel()
    {
        return totalLevel;
    }

    public static int[] getExperience()
    {
        return Arrays.copyOf(experience, experience.length);
    }

    public static int[] getLevels()
    {
        return Arrays.copyOf(realLevels, realLevels.length);
    }

    public static int[] getBoostedLevels()
    {
        return Arrays.copyOf(boostedLevels, boostedLevels.length);
    }

    public static void refresh()
    {
        int[] nextReal = new int[Skill.values().length];
        int[] nextBoosted = new int[Skill.values().length];
        int[] nextExperience = new int[Skill.values().length];
        for (Skill skill : Skill.values())
        {
            int index = skill.ordinal();
            nextReal[index] = DreamBotRebornApi.requireClient().getRealSkillLevel(skill.unwrap());
            nextBoosted[index] = DreamBotRebornApi.requireClient().getBoostedSkillLevel(skill.unwrap());
            nextExperience[index] = DreamBotRebornApi.requireClient().getSkillExperience(skill.unwrap());
        }
        realLevels = nextReal;
        boostedLevels = nextBoosted;
        experience = nextExperience;
        totalLevel = DreamBotRebornApi.requireClient().getTotalLevel();
    }

    public static void clear()
    {
        realLevels = new int[Skill.values().length];
        boostedLevels = new int[Skill.values().length];
        experience = new int[Skill.values().length];
        totalLevel = 0;
    }
}
