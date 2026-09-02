package com.dreambotreborn.api.methods.settings;

import com.dreambotreborn.api.DreamBotRebornApi;

/** Client-local integer and string variables. */
public final class Varcs
{
    private Varcs()
    {
    }

    public static int getInt(int id)
    {
        return DreamBotRebornApi.requireClient().getVarcIntValue(id);
    }

    public static String getString(int id)
    {
        return DreamBotRebornApi.requireClient().getVarcStrValue(id);
    }

    public static void setInt(int id, int value)
    {
        DreamBotRebornApi.requireClient().setVarcIntValue(id, value);
    }

    public static void setString(int id, String value)
    {
        DreamBotRebornApi.requireClient().setVarcStrValue(id, value);
    }
}
