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

    /** Converts DreamBot's boxed ID varargs without changing primitive-array ABI calls. */
    public static int[] unboxIds(Integer... ids)
    {
        if (ids == null) return null;
        int[] result = new int[ids.length];
        for (int index = 0; index < ids.length; index++)
            result[index] = ids[index] == null ? Integer.MIN_VALUE : ids[index];
        return result;
    }
}
