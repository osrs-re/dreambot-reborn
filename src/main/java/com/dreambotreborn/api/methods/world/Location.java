package com.dreambotreborn.api.methods.world;

import java.util.Locale;

public enum Location
{
    USA(0, ""),
    USA_EAST(0, "east"),
    USA_WEST(0, "west"),
    UK(1, "uk"),
    AUSTRALIA(3, "aus"),
    GERMANY(7, "germany");

    private final int location;
    private final String host;

    Location(int location, String host)
    {
        this.location = location;
        this.host = host;
    }

    public int getLocation()
    {
        return location;
    }

    public String getHost()
    {
        return host;
    }

    public boolean matches(int value, String worldHost)
    {
        if (value != location) return false;
        if (host.isEmpty()) return this == USA;
        return worldHost != null && worldHost.toLowerCase(Locale.ENGLISH).contains(host);
    }

    public static Location getForTexture(int texture)
    {
        for (Location location : values())
        {
            if (location.location == texture && location != USA_EAST && location != USA_WEST)
                return location;
        }
        return USA;
    }
}
