package com.dreambotreborn.api.methods.world;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Immutable world-list entry. */
public final class World
{
    public int id;
    public int population;
    public int location;
    public String activity;
    public String host;
    public Set<WorldType> types;
    private int ping = -1;
    private int minimumLevel;
    private boolean suspicious;

    private final net.runelite.api.World world;

    public World()
    {
        this.world = null;
        this.activity = "";
        this.host = "";
        this.types = Collections.emptySet();
    }

    public World(int id, boolean member, boolean pvp, boolean highRisk,
                 boolean deadman, int location, int minimumLevel,
                 boolean tournament, boolean lastManStanding,
                 boolean speedRunning, boolean freshStart)
    {
        this();
        this.id = id;
        this.location = location;
        this.minimumLevel = minimumLevel;
        EnumSet<WorldType> values = EnumSet.noneOf(WorldType.class);
        if (member) values.add(WorldType.MEMBER);
        if (pvp) values.add(WorldType.PVP);
        if (highRisk) values.add(WorldType.HIGH_RISK);
        if (deadman) values.add(WorldType.DEADMAN);
        if (tournament) values.add(WorldType.TOURNAMENT);
        if (lastManStanding) values.add(WorldType.LMS);
        if (speedRunning) values.add(WorldType.SPEED_RUN);
        if (freshStart) values.add(WorldType.FRESH_START);
        types = values;
    }

    public World(net.runelite.api.World world)
    {
        this.world = world;
        this.id = world.getId();
        this.population = world.getPlayerCount();
        this.location = world.getLocation();
        this.activity = world.getActivity() == null ? "" : world.getActivity();
        this.host = world.getAddress() == null ? "" : world.getAddress();
        EnumSet<WorldType> source = WorldType.fromRuneLite(world.getTypes());
        this.types = source.isEmpty()
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
        if (minimumLevel > 0) return minimumLevel;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("(?:skill total|level|levels?)\\D*(\\d+)|(\\d+)\\s*\\+",
                java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(activity);
        if (!matcher.find()) return 0;
        String value = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
        try { minimumLevel = Integer.parseInt(value); return minimumLevel; }
        catch (NumberFormatException ignored) { return 0; }
    }

    public boolean isMembers()
    {
        return types.contains(WorldType.MEMBER);
    }

    public boolean isF2P()
    {
        return !isMembers();
    }

    public boolean isPVP()
    {
        return types.contains(WorldType.PVP) || types.contains(WorldType.HIGH_RISK)
            || types.contains(WorldType.DEADMAN) || types.contains(WorldType.TARGET);
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
        return types.contains(WorldType.SPEED_RUN);
    }

    public boolean isLastManStanding()
    {
        return types.contains(WorldType.LMS);
    }

    public boolean isTournamentWorld()
    {
        return types.contains(WorldType.TOURNAMENT);
    }

    public boolean isFreshStart()
    {
        return types.contains(WorldType.FRESH_START);
    }

    public boolean isBeta()
    {
        return types.contains(WorldType.BETA);
    }

    public boolean isNormal()
    {
        for (WorldType type : types)
        {
            if (type != WorldType.MEMBER)
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

    public int getPing() { return ping; }
    public void setPing(int value) { ping = value; }
    public Location getLocation() { return Location.getForTexture(location); }
    public Location getExactLocation()
    {
        if (Location.USA_EAST.matches(location, host)) return Location.USA_EAST;
        if (Location.USA_WEST.matches(location, host)) return Location.USA_WEST;
        return getLocation();
    }
    public String getRealHost() { return host; }
    public void setRealHost(String value) { host = value == null ? "" : value; }
    public boolean isTargetWorld() { return types.contains(WorldType.TARGET); }
    public boolean isLeagueWorld() { return types.contains(WorldType.TWISTED); }
    public boolean isPvpArena() { return types.contains(WorldType.PVP_ARENA); }
    public boolean isSuspicious() { return suspicious; }
    public int getMask()
    {
        int mask = 0;
        for (WorldType type : types) if (type != WorldType.UNKNOWN) mask |= type.getMaskValue();
        return mask;
    }
    public void setDescription(String value) { activity = value == null ? "" : value; }
    public void setHost(String value) { host = value == null ? "" : value; }
    public void setRealId(int value) { id = value; }
    public void setLocation(int value) { location = value; }
    public void setMinLevel(int value) { minimumLevel = Math.max(0, value); }
    public void setPopulation(int value) { population = value; }
    public void setSuspicious(boolean value) { suspicious = value; }
    public void setMask(int value) { types = WorldType.fromMask(value); }
    public void setMember(boolean value) { setType(WorldType.MEMBER, value); }
    public void setPVP(boolean value) { setType(WorldType.PVP, value); }
    public void setHighRisk(boolean value) { setType(WorldType.HIGH_RISK, value); }
    public void setDeadmanMode(boolean value) { setType(WorldType.DEADMAN, value); }
    public void setSpeedRunning(boolean value) { setType(WorldType.SPEED_RUN, value); }
    public void setLastManStanding(boolean value) { setType(WorldType.LMS, value); }
    public void setTournamentWorld(boolean value) { setType(WorldType.TOURNAMENT, value); }
    public void setTargetWorld(boolean value) { setType(WorldType.TARGET, value); }
    public void setLeagueWorld(boolean value) { setType(WorldType.TWISTED, value); }
    public void setPvpArena(boolean value) { setType(WorldType.PVP_ARENA, value); }
    public void setFreshStart(boolean value) { setType(WorldType.FRESH_START, value); }
    public void setBeta(boolean value) { setType(WorldType.BETA, value); }

    private void setType(WorldType type, boolean enabled)
    {
        EnumSet<WorldType> copy = types.isEmpty()
            ? EnumSet.noneOf(WorldType.class) : EnumSet.copyOf(types);
        if (enabled) copy.add(type); else copy.remove(type);
        types = copy;
    }

    @Override
    public String toString()
    {
        return "World{" + "id=" + id + ", population=" + population +
            ", activity='" + activity + '\'' + ", types=" + types + '}';
    }
}
