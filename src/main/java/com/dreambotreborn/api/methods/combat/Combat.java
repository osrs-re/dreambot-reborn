package com.dreambotreborn.api.methods.combat;

import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.wrappers.interactive.Player;

/** Read-only combat status modeled after DreamBot's Combat helper. */
public final class Combat
{
    private Combat()
    {
    }

    public static int getCombatLevel()
    {
        Player local = Players.getLocal();
        return local == null ? 0 : local.combatLevel;
    }

    public static boolean isPoisoned()
    {
        int poison = DreamBotRebornApi.requireClient().getVarpValue(VarPlayer.POISON);
        return poison > 0 && poison < 1_000_000;
    }

    public static boolean isEnvenomed()
    {
        return DreamBotRebornApi.requireClient().getVarpValue(VarPlayer.POISON) >= 1_000_000;
    }

    public static boolean isSpecialActive()
    {
        return DreamBotRebornApi.requireClient().getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED) == 1;
    }

    public static int getSpecialPercentage()
    {
        return DreamBotRebornApi.requireClient().getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT) / 10;
    }

    public static boolean isInWild()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.IN_WILDERNESS) == 1;
    }

    public static boolean isInMultiCombat()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.MULTICOMBAT_AREA) == 1;
    }

    public static int getHealthPercent()
    {
        Player local = Players.getLocal();
        return local == null ? -1 : local.healthPercent;
    }
}
