package com.dreambotreborn.api.methods.settings;

import java.util.Arrays;
import com.dreambotreborn.api.DreamBotRebornApi;

/** Varp and varbit access. */
public final class PlayerSettings
{
    private PlayerSettings()
    {
    }

    public static int getConfig(int id)
    {
        return DreamBotRebornApi.requireClient().getVarpValue(id);
    }

    public static int getBitValue(int id)
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(id);
    }

    public static int getCleanedConfig(int id, int mask)
    {
        return getConfig(id) & mask;
    }

    public static int[] getConfigs()
    {
        int[] values = DreamBotRebornApi.requireClient().getVarps();
        return values == null ? new int[0] : Arrays.copyOf(values, values.length);
    }
}
