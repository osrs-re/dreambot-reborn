package com.dreambotreborn.api.methods.skills;

import java.util.EnumMap;
import java.util.Map;

/** Per-skill XP, level and rate tracking for script paints. */
public final class SkillTracker
{
    private static final Map<Skill, Start> STARTS = new EnumMap<>(Skill.class);

    private SkillTracker() { }

    public static synchronized void start() { start(Skill.values()); }
    public static synchronized void start(boolean reset)
    {
        for (Skill skill : Skill.values()) start(skill, reset);
    }
    public static synchronized void start(Skill skill) { start(skill, false); }
    public static synchronized void start(Skill... skills)
    {
        if (skills != null) for (Skill skill : skills) start(skill, false);
    }
    public static synchronized void start(Skill skill, boolean reset)
    {
        if (skill != null && (reset || !STARTS.containsKey(skill)))
            STARTS.put(skill, new Start(System.currentTimeMillis(),
                Skills.getExperience(skill), Skills.getRealLevel(skill)));
    }
    public static synchronized void reset(Skill skill)
    {
        if (skill != null) STARTS.remove(skill);
    }
    public static synchronized void resetAll() { STARTS.clear(); }
    public static synchronized long getGainedExperience(Skill skill)
    {
        Start start = STARTS.get(skill);
        return start == null ? 0L : Math.max(0L, (long) Skills.getExperience(skill) - start.experience);
    }
    public static synchronized int getGainedExperiencePerHour(Skill skill)
    {
        Start start = STARTS.get(skill);
        if (start == null) return 0;
        long elapsed = Math.max(1L, System.currentTimeMillis() - start.time);
        return (int) Math.min(Integer.MAX_VALUE, getGainedExperience(skill) * 3_600_000L / elapsed);
    }
    public static synchronized long getTimeToLevel(Skill skill)
    {
        int hourly = getGainedExperiencePerHour(skill);
        if (hourly <= 0) return -1L;
        return (long) Math.ceil(Skills.getExperienceToLevel(skill) * 3_600_000.0 / hourly);
    }
    public static synchronized long getStartTime(Skill skill)
    {
        Start start = STARTS.get(skill);
        return start == null ? -1L : start.time;
    }
    public static synchronized int getStartExperience(Skill skill)
    {
        Start start = STARTS.get(skill);
        return start == null ? -1 : start.experience;
    }
    public static synchronized int getStartLevel(Skill skill)
    {
        Start start = STARTS.get(skill);
        return start == null ? -1 : start.level;
    }
    public static synchronized int getGainedLevels(Skill skill)
    {
        Start start = STARTS.get(skill);
        return start == null ? 0 : Math.max(0, Skills.getRealLevel(skill) - start.level);
    }
    public static synchronized boolean hasStarted(Skill skill) { return STARTS.containsKey(skill); }

    private static final class Start
    {
        private final long time;
        private final int experience;
        private final int level;
        private Start(long time, int experience, int level)
        {
            this.time = time;
            this.experience = experience;
            this.level = level;
        }
    }
}
