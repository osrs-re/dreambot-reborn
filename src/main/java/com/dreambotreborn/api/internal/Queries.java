package com.dreambotreborn.api.internal;

public final class Queries
{
    private Queries()
    {
    }

    public static boolean name(String value, String... names)
    {
        if (names != null)
        {
            for (String name : names)
            {
                if (name != null && value.equalsIgnoreCase(name))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean id(int value, int... ids)
    {
        if (ids != null)
        {
            for (int id : ids)
            {
                if (value == id)
                {
                    return true;
                }
            }
        }
        return false;
    }
}
