package com.dreambotreborn.api.methods.magic;

import java.util.Locale;
import com.dreambotreborn.api.methods.magic.cost.Rune;

final class SpellData
{
    private static final double[] NORMAL_EXPERIENCE =
    {
        0, 5.5, 13, 9, 7.5, 17.5, 9.5, 21, 11.5, 25, 13.5, 29, 30, 31,
        16.5, 35, 37, 19.5, 41, 43, 22.5, 47, 24.5, 30, 25.5, 53, 55.5,
        28.5, 58, 59, 30, 60, 30, 61, 31.5, 64, 65, 56, 67, 68, 34.5, 70,
        65, 61, 61, 61, 68, 36, 73, 74, 37.5, 76, 76, 78, 40, 83, 84, 42.5,
        89, 90, 180, 44.5, 92, 46.5, 80, 80, 97, 100, 48.5, 110, 50.5, 58
    };
    private static final double[] ANCIENT_EXPERIENCE =
    {
        0, 34, 46, 40, 52, 33, 45, 39, 51, 30, 42, 36, 48, 31, 43, 37, 49,
        64, 70, 76, 82, 88, 94, 100, 106, 64, 94
    };
    private static final double[] LUNAR_EXPERIENCE =
    {
        0, 60, 60, 61, 63, 65, 65, 66, 67, 69, 70, 71, 72, 74, 76, 76, 77,
        78, 81, 80, 81, 82, 83, 84, 86, 87, 88, 89, 90, 90, 92, 93, 97.5,
        96, 99, 100, 101, 108, 112, 124, 130, 60, 75, 69
    };
    private static final double[] ARCEUUS_MAGIC_EXPERIENCE =
    {
        0, 9, 32, 16, 22, 22, 27, 74, 74, 74, 74, 30, 92, 27, 50, 58, 114,
        114, 114, 114, 114, 114, 68, 130, 130, 74, 140, 150, 82, 156, 160,
        160, 160, 160, 90, 150, 150, 160, 90, 160, 170, 180, 100, 200
    };
    private static final double[] ARCEUUS_PRAYER_EXPERIENCE =
    {
        0, 0, 364, 0, 0, 0, 0, 0, 0, 0, 0, 0, 780, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 1100, 0, 1100, 1100, 1100, 0, 0, 0, 0,
        0, 0, 0, 1500, 0, 0
    };

    private SpellData() { }

    static double normalExperience(Normal spell) { return NORMAL_EXPERIENCE[spell.ordinal()]; }
    static double ancientExperience(Ancient spell) { return ANCIENT_EXPERIENCE[spell.ordinal()]; }
    static double lunarExperience(Lunar spell) { return LUNAR_EXPERIENCE[spell.ordinal()]; }
    static double arceuusMagicExperience(Arceuus spell)
    { return ARCEUUS_MAGIC_EXPERIENCE[spell.ordinal()]; }
    static double arceuusPrayerExperience(Arceuus spell)
    { return ARCEUUS_PRAYER_EXPERIENCE[spell.ordinal()]; }
    static Rune[] costs(String encoded)
    {
        if (encoded == null || encoded.isEmpty()) return new Rune[0];
        String[] entries = encoded.split(",");
        Rune[] result = new Rune[entries.length];
        for (int i = 0; i < entries.length; i++)
        {
            String[] pair = entries[i].split(":", 2);
            String name = pair[0].replace('_', ' ').toLowerCase(Locale.ENGLISH);
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            if (!name.endsWith("rune") && !name.equals("Unpowered orb") && !name.equals("Banana"))
                name += " rune";
            result[i] = new Rune(name, pair.length == 1 ? 1 : Integer.parseInt(pair[1]));
        }
        return result;
    }

    static String title(Enum<?> value)
    {
        String text = value.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
        StringBuilder result = new StringBuilder(text.length()); boolean upper = true;
        for (char c : text.toCharArray()) { result.append(upper ? Character.toUpperCase(c) : c); upper = c == ' '; }
        return result.toString();
    }

    static Rune[] normal(Normal spell)
    {
        switch (spell)
        {
            case WIND_STRIKE: return costs("Mind:1,Air:1");
            case CONFUSE: return costs("Body:1,Earth:2,Water:3");
            case ENCHANT_CROSSBOW_BOLT: return costs("Cosmic:1,Air:2");
            case WATER_STRIKE: return costs("Mind:1,Water:1,Air:1");
            case LEVEL_1_ENCHANT: return costs("Cosmic:1,Water:1");
            case EARTH_STRIKE: return costs("Mind:1,Earth:2,Air:1");
            case WEAKEN: return costs("Body:1,Earth:2,Water:3");
            case FIRE_STRIKE: return costs("Mind:1,Fire:3,Air:2");
            case BONES_TO_BANANAS: return costs("Nature:1,Earth:2,Water:2");
            case WIND_BOLT: return costs("Chaos:1,Air:2");
            case CURSE: return costs("Body:1,Earth:3,Water:2");
            case BIND: return costs("Nature:2,Earth:3,Water:3");
            case LOW_LEVEL_ALCHEMY: return costs("Nature:1,Fire:3");
            case WATER_BOLT: return costs("Chaos:1,Water:2,Air:2");
            case VARROCK_TELEPORT: return costs("Law:1,Fire:1,Air:3");
            case LEVEL_2_ENCHANT: return costs("Cosmic:1,Air:3");
            case EARTH_BOLT: return costs("Chaos:1,Earth:3,Air:2");
            case LUMBRIDGE_TELEPORT: return costs("Law:1,Earth:1,Air:3");
            case TELEKINETIC_GRAB: return costs("Law:1,Air:1");
            case FIRE_BOLT: return costs("Chaos:1,Fire:4,Air:3");
            case FALADOR_TELEPORT: return costs("Law:1,Water:1,Air:3");
            case CRUMBLE_UNDEAD: return costs("Chaos:1,Earth:2,Air:2");
            case TELEPORT_TO_HOUSE: return costs("Law:1,Earth:1,Air:1");
            case WIND_BLAST: return costs("Death:1,Air:3");
            case SUPERHEAT_ITEM: return costs("Nature:1,Fire:4");
            case CAMELOT_TELEPORT: return costs("Law:1,Air:5");
            case WATER_BLAST: return costs("Death:1,Water:3,Air:3");
            case KOUREND_CASTLE_TELEPORT:
            case TELEPORT_TO_KOUREND: return costs("Fire:1,Water:1,Law:2");
            case LEVEL_3_ENCHANT: return costs("Cosmic:1,Fire:5");
            case IBAN_BLAST: return costs("Death:1,Fire:5");
            case SNARE: return costs("Nature:3,Earth:4,Water:4");
            case MAGIC_DART: return costs("Death:1,Mind:4");
            case ARDOUGNE_TELEPORT: return costs("Law:2,Water:2");
            case EARTH_BLAST: return costs("Death:1,Earth:4,Air:3");
            case CIVITAS_ILLA_FORTIS_TELEPORT: return costs("Law:2,Earth:1,Fire:1");
            case HIGH_LEVEL_ALCHEMY: return costs("Nature:1,Fire:5");
            case CHARGE_WATER_ORB: return costs("Cosmic:3,Water:30,Unpowered_orb:1");
            case LEVEL_4_ENCHANT: return costs("Cosmic:1,Earth:10");
            case WATCHTOWER_TELEPORT: return costs("Law:2,Earth:2");
            case FIRE_BLAST: return costs("Death:1,Fire:5,Air:4");
            case CHARGE_EARTH_ORB: return costs("Cosmic:3,Earth:30,Unpowered_orb:1");
            case BONES_TO_PEACHES: return costs("Nature:2,Earth:2,Water:4");
            case SARADOMIN_STRIKE: return costs("Fire:2,Blood:2,Air:4");
            case CLAWS_OF_GUTHIX: return costs("Blood:2,Fire:1,Air:4");
            case FLAMES_OF_ZAMORAK: return costs("Blood:2,Fire:4,Air:1");
            case TROLLHEIM_TELEPORT: return costs("Law:2,Fire:2");
            case WIND_WAVE: return costs("Blood:1,Air:5");
            case CHARGE_FIRE_ORB: return costs("Cosmic:3,Fire:30,Unpowered_orb:1");
            case APE_ATOLL_TELEPORT: return costs("Law:2,Fire:2,Water:2,Banana:1");
            case WATER_WAVE: return costs("Blood:1,Water:7,Air:5");
            case CHARGE_AIR_ORB: return costs("Cosmic:3,Air:30,Unpowered_orb:1");
            case VULNERABILITY: return costs("Soul:1,Earth:5,Water:5");
            case LEVEL_5_ENCHANT: return costs("Cosmic:1,Earth:15,Water:15");
            case EARTH_WAVE: return costs("Blood:1,Earth:7,Air:5");
            case ENFEEBLE: return costs("Soul:1,Earth:8,Water:8");
            case TELEOTHER_LUMBRIDGE: return costs("Soul:1,Law:1,Earth:1");
            case FIRE_WAVE: return costs("Blood:1,Fire:7,Air:5");
            case ENTANGLE: return costs("Nature:4,Earth:5,Water:5");
            case STUN: return costs("Soul:1,Earth:12,Water:12");
            case CHARGE: return costs("Blood:3,Fire:3,Air:3");
            case WIND_SURGE: return costs("Air:7,Wrath:1");
            case TELEOTHER_FALADOR: return costs("Soul:1,Law:1,Water:1");
            case WATER_SURGE: return costs("Water:10,Air:7,Wrath:1");
            case TELE_BLOCK:
            case TELEPORT_TO_TARGET: return costs("Law:1,Death:1,Chaos:1");
            case LEVEL_6_ENCHANT: return costs("Cosmic:1,Fire:20,Earth:20");
            case TELEOTHER_CAMELOT: return costs("Soul:2,Law:1");
            case EARTH_SURGE: return costs("Earth:10,Air:7,Wrath:1");
            case LEVEL_7_ENCHANT: return costs("Soul:20,Blood:20,Cosmic:1");
            case FIRE_SURGE: return costs("Fire:10,Air:7,Wrath:1");
            default: return new Rune[0];
        }
    }

    static int normalMaxHit(Normal spell)
    {
        switch (spell)
        {
            case WIND_STRIKE: return 2; case WATER_STRIKE: return 4; case EARTH_STRIKE: return 6;
            case FIRE_STRIKE: return 8; case WIND_BOLT: return 9; case WATER_BOLT: return 10;
            case EARTH_BOLT: return 11; case FIRE_BOLT: return 12; case WIND_BLAST: return 13;
            case WATER_BLAST: return 14; case EARTH_BLAST: case CRUMBLE_UNDEAD: case MAGIC_DART: return 15;
            case FIRE_BLAST: return 16; case WIND_WAVE: return 17; case WATER_WAVE: return 18;
            case EARTH_WAVE: return 19; case FIRE_WAVE: case SARADOMIN_STRIKE:
            case CLAWS_OF_GUTHIX: case FLAMES_OF_ZAMORAK: return 20;
            case WIND_SURGE: return 21; case WATER_SURGE: return 22; case EARTH_SURGE: return 23;
            case FIRE_SURGE: return 24; case IBAN_BLAST: return 25; case SNARE: return 3;
            case ENTANGLE: return 5; default: return 0;
        }
    }
}
