package com.dreambotreborn.api.methods.world;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.api.WorldType;

/** Immutable world-list entry. */
public final class World
{
    public final int id;
    public final int population;
    public final int location;
    public final String activity;
    public final String host;
    public final Set<WorldType> types;

    private final net.runelite.api.World world;

    public World(net.runelite.api.World world)
    {
        this.world = world;
        this.id = world.getId();
        this.population = world.getPlayerCount();
        this.location = world.getLocation();
        this.activity = world.getActivity() == null ? "" : world.getActivity();
        this.host = world.getAddress() == null ? "" : world.getAddress();
        EnumSet<WorldType> source = world.getTypes();
        this.types = source == null || source.isEmpty()
            ? Collections.emptySet()
            : Collections.unmodifiableSet(EnumSet.copyOf(source));
    }

    public int getWorld()
    {
        return id;
    }

    public int getRealID()
    {
        return id;
    }

    public int getRealId()
    {
        return id;
    }

    public int getPopulation()
    {
        return population;
    }

    public int getLocationValue()
    {
        return location;
    }

    public String getDescription()
    {
        return activity;
    }

    public String getHost()
    {
        return host;
    }

    public int getMinimumLevel()
    {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("(?:skill total|level|levels?)\\D*(\\d+)|(\\d+)\\s*\\+",
                java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(activity);
        if (!matcher.find()) return 0;
        String value = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return 0; }
    }

    public boolean isMembers()
    {
        return types.contains(WorldType.MEMBERS);
    }

    public boolean isF2P()
    {
        return !isMembers();
    }

    public boolean isPVP()
    {
        return WorldType.isPvpWorld(types);
    }

    public boolean isHighRisk()
    {
        return types.contains(WorldType.HIGH_RISK);
    }

    public boolean isDeadmanMode()
    {
        return types.contains(WorldType.DEADMAN);
    }

    public boolean isSpeedRunning()
    {
        return types.contains(WorldType.QUEST_SPEEDRUNNING);
    }

    public boolean isLastManStanding()
    {
        return types.contains(WorldType.LAST_MAN_STANDING);
    }

    public boolean isTournamentWorld()
    {
        return types.contains(WorldType.TOURNAMENT_WORLD);
    }

    public boolean isFreshStart()
    {
        return types.contains(WorldType.FRESH_START_WORLD);
    }

    public boolean isBeta()
    {
        return types.contains(WorldType.BETA_WORLD);
    }

    public boolean isNormal()
    {
        for (WorldType type : types)
        {
            if (type != WorldType.MEMBERS)
            {
                return false;
            }
        }
        return true;
    }

    public net.runelite.api.World unwrap()
    {
        return world;
    }

    @Override
    public String toString()
    {
        return "World{" + "id=" + id + ", population=" + population +
            ", activity='" + activity + '\'' + ", types=" + types + '}';
    }
}
