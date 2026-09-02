package com.dreambotreborn.api.methods.magic;

import java.util.Locale;
import com.dreambotreborn.api.methods.magic.cost.Rune;

/** Standard spellbook. Child ids track RuneLite's current gameval interface. */
public enum Normal implements Spell
{
    HOME_TELEPORT(6, 0), WIND_STRIKE(11, 1), CONFUSE(12, 3),
    ENCHANT_CROSSBOW_BOLT(13, 4), WATER_STRIKE(14, 5), LEVEL_1_ENCHANT(16, 7),
    EARTH_STRIKE(17, 9), WEAKEN(18, 11), FIRE_STRIKE(19, 13),
    BONES_TO_BANANAS(20, 15), WIND_BOLT(21, 17), CURSE(22, 19), BIND(23, 20),
    LOW_LEVEL_ALCHEMY(24, 21), WATER_BOLT(25, 23), VARROCK_TELEPORT(26, 25),
    LEVEL_2_ENCHANT(27, 27), EARTH_BOLT(28, 29), LUMBRIDGE_TELEPORT(29, 31),
    TELEKINETIC_GRAB(30, 33), FIRE_BOLT(31, 35), FALADOR_TELEPORT(32, 37),
    CRUMBLE_UNDEAD(33, 39), TELEPORT_TO_HOUSE(34, 40), WIND_BLAST(35, 41),
    SUPERHEAT_ITEM(36, 43), CAMELOT_TELEPORT(37, 45), WATER_BLAST(38, 47),
    KOUREND_CASTLE_TELEPORT(39, 48), LEVEL_3_ENCHANT(40, 49), IBAN_BLAST(41, 50),
    SNARE(42, 50), MAGIC_DART(43, 50), ARDOUGNE_TELEPORT(44, 51),
    EARTH_BLAST(45, 53), CIVITAS_ILLA_FORTIS_TELEPORT(46, 54),
    HIGH_LEVEL_ALCHEMY(47, 55), CHARGE_WATER_ORB(48, 56), LEVEL_4_ENCHANT(49, 57),
    WATCHTOWER_TELEPORT(50, 58), FIRE_BLAST(51, 59), CHARGE_EARTH_ORB(52, 60),
    BONES_TO_PEACHES(53, 60), SARADOMIN_STRIKE(54, 60), CLAWS_OF_GUTHIX(55, 60),
    FLAMES_OF_ZAMORAK(56, 60), TROLLHEIM_TELEPORT(57, 61), WIND_WAVE(58, 62),
    CHARGE_FIRE_ORB(59, 63), APE_ATOLL_TELEPORT(60, 64), WATER_WAVE(61, 65),
    CHARGE_AIR_ORB(62, 66), VULNERABILITY(63, 66), LEVEL_5_ENCHANT(64, 68),
    EARTH_WAVE(65, 70), ENFEEBLE(66, 73), TELEOTHER_LUMBRIDGE(67, 74),
    FIRE_WAVE(68, 75), ENTANGLE(69, 79), STUN(70, 80), CHARGE(71, 80),
    WIND_SURGE(72, 81), TELEOTHER_FALADOR(73, 82), WATER_SURGE(74, 85),
    TELE_BLOCK(75, 85), TELEPORT_TO_TARGET(76, 85), LEVEL_6_ENCHANT(77, 87),
    TELEOTHER_CAMELOT(78, 90), EARTH_SURGE(79, 90), LEVEL_7_ENCHANT(80, 93),
    FIRE_SURGE(81, 95), TELEPORT_TO_KOUREND(82, 69);

    private static final int PARENT = 218;
    private final int child;
    private final int level;
    Normal(int child, int level) { this.child = child; this.level = level; }
    public int getChild() { return child; }
    public int getParent() { return PARENT; }
    public int getAutocastId() { return ordinal(); }
    public Rune[] getCost() { return new Rune[0]; }
    public int getLevel() { return level; }
    public int getMaxHit() { return 0; }
    public double getExperience() { return 0.0; }
    @Override public String toString()
    {
        String value = name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
