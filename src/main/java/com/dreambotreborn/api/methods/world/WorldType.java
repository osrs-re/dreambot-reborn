package com.dreambotreborn.api.methods.world;

import java.util.EnumSet;

/** DreamBot world masks with conversion from RuneLite's typed world flags. */
public enum WorldType
{
    MEMBER(1),
    PVP(4),
    PRIVATE(16),
    TARGET(32),
    PVP_ARENA(64),
    SKILL(128),
    SPEED_RUN(256),
    HIGH_RISK(1024),
    LMS(16384),
    TESTING(0x02000000),
    SOUL_WARS(0x04000000),
    WILDY_DYNAMITE(0x08000000),
    BETA(0x1000000),
    TOURNAMENT(0x2000000),
    FRESH_START(0x4000000),
    SOMETHING(0x8000000),
    DEADMAN(0x20000000),
    TWISTED(0x40000000),
    UNKNOWN(-1);

    private final int maskValue;

    WorldType(int maskValue)
    {
        this.maskValue = maskValue;
    }

    public int getMaskValue()
    {
        return maskValue;
    }

    public static EnumSet<WorldType> fromMask(int mask)
    {
        EnumSet<WorldType> result = EnumSet.noneOf(WorldType.class);
        for (WorldType type : values())
        {
            if (type != UNKNOWN && (mask & type.maskValue) != 0) result.add(type);
        }
        return result;
    }

    static EnumSet<WorldType> fromRuneLite(java.util.Set<net.runelite.api.WorldType> source)
    {
        EnumSet<WorldType> result = EnumSet.noneOf(WorldType.class);
        if (source == null) return result;
        for (net.runelite.api.WorldType type : source)
        {
            switch (type)
            {
                case MEMBERS: result.add(MEMBER); break;
                case PVP: result.add(PVP); break;
                case BOUNTY: result.add(TARGET); break;
                case PVP_ARENA: result.add(PVP_ARENA); break;
                case SKILL_TOTAL: result.add(SKILL); break;
                case QUEST_SPEEDRUNNING: result.add(SPEED_RUN); break;
                case HIGH_RISK: result.add(HIGH_RISK); break;
                case LAST_MAN_STANDING: result.add(LMS); break;
                case BETA_WORLD: result.add(BETA); break;
                case TOURNAMENT_WORLD: result.add(TOURNAMENT); break;
                case FRESH_START_WORLD: result.add(FRESH_START); break;
                case DEADMAN: result.add(DEADMAN); break;
                case SEASONAL: result.add(TWISTED); break;
                default: result.add(UNKNOWN); break;
            }
        }
        return result;
    }
}
