package com.dreambotreborn.api.methods.container.impl.bank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.dreambotreborn.api.Client;
import com.dreambotreborn.api.methods.map.Area;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.utilities.impl.Condition;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Locatable;

/** Known OSRS bank locations, preserved under the DreamBot API names. */
public enum BankLocation implements Locatable
{
    AL_KHARID(3270,3167,0,false), ALDARIN(1398,2927,0,true),
    ARCEUUS(1635,3746,0,true), ARDOUGNE_NORTH(2617,3333,0,true),
    ARDOUGNE_SOUTH(2653,3282,0,true), AUBURNVALE(1413,3353,0,true),
    BARBARIAN_OUTPOST(2536,3573,0,true), BLAST_FURNACE(1948,4957,0,true),
    BURGH_DE_ROTT(3497,3211,0,true), CANIFIS(3512,3479,0,true),
    CASTLE_WARS(2442,3084,0,false), CATHERBY(2809,3440,0,true),
    CIVITAS_ILLA_FORTIS_EAST(1779,3095,0,true), CIVITAS_ILLA_FORTIS_WEST(1647,3116,0,true),
    CORSAIR_COVE(2570,2863,0,false), CRAFTING_GUILD(2936,3280,0,true),
    DARKMEYER(3605,3366,0,true), DRAYNOR(3093,3243,0,false),
    DUEL_ARENA(3382,3268,0,true), EDGEVILLE(3093,3491,0,false),
    ETCETERIA(2618,3895,0,true), FALADOR_EAST(3013,3356,0,false),
    FALADOR_WEST(2946,3370,0,false), FARMING_GUILD(1253,3741,0,true),
    FEROX_ENCLAVE(3130,3631,0,false), FISHING_GUILD(2588,3419,0,true),
    FOSSIL_ISLAND(3740,3803,0,true), GIANTS_FOUNDRY(3375,11493,0,true),
    GNOME_STRONGHOLD(2445,3423,1,true), GOTR(3618,9473,0,false),
    GRAND_EXCHANGE(3165,3489,0,false,BankType.EXCHANGE), GRAND_TREE(2449,3482,1,true),
    HOSIDIUS(1749,3598,0,true), HOSIDIUS_SHORE(1719,3465,0,true),
    HOSIDIUS_KITCHEN(1678,3617,0,true), HOSIDIUS_VINERY(1808,3566,0,true),
    HUNTER_GUILD(1543,3041,0,true), JATIZSO(2416,3800,0,true),
    KELDAGRIM(2838,10210,0,true), KOUREND_CASTLE(1612,3681,2,true),
    LANDS_END(1510,3420,0,true), LLETYA(2351,3165,0,true),
    LOVAKENGJ_SOUTH(1526,3739,0,true), LOVAKENGJ_WEST(1437,3823,0,true),
    LUMBRIDGE(3208,3218,2,false), LUMBRIDGE_BASEMENT(3218,9623,0,true),
    LUNAR_ISLAND(2100,3917,0,true), MAGE_BANK(2534,4712,0,true,BankType.CHEST),
    MAGE_TRAINING_ARENA(3364,3318,1,true), MINING_GUILD(3014,9718,0,true),
    MISTROCK(1382,2866,0,true), MIXOLOGY(1398,9313,0,true),
    MOONCLAN_ISLE(2100,3918,0,true), MOS_LEHARMLESS(3680,2982,0,true),
    MOTHERLODE_MINE(3760,5666,0,true,BankType.CHEST), MOUNT_KARUULM(1324,3824,0,true),
    MOUNT_QUIDAMORTEM(1253,3571,0,true), MYTHS_GUILD(2465,2848,1,true),
    NARDAH(3429,2891,0,true), NEITIZNOT(2336,3807,0,true),
    PEST_CONTROL(2667,2653,0,true), PISCATORIS(2330,3688,0,true),
    PORT_KHAZARD(2662,3161,0,true), PORT_PHASMATYS(3688,3466,0,true),
    PORT_PISCARILIUS(1803,3788,0,true), ROGUES_DEN(3043,4972,1,true,BankType.NPC),
    RUINS_OF_CAMDOZAAL(2978,5798,0,false), RUINS_OF_UNKAH(3155,2835,0,true),
    SEERS(2726,3492,0,true), SHANTAY_PASS(3303,3123,0,true,BankType.CHEST),
    SHAYZIEN(1489,3592,0,true), SHILO_VILLAGE(2852,2955,0,true),
    SOUL_WARS(2212,2860,0,false), TAL_TEKLAN(1243,3121,0,true),
    TZHAAR(2446,5178,0,true), TZHAAR_EAST(2543,5139,0,true),
    VARROCK_EAST(3253,3421,0,false), VARROCK_WEST(3183,3440,0,false),
    VER_SINHAZA(3650,3210,0,true), NORTH_PRIFDDINAS(3257,6107,0,true),
    SOUTH_PRIFDDINAS(3295,6059,0,true), WARRIORS_GUILD(2844,3542,0,true),
    WINTERTODT(1640,3944,0,false), WOODCUTTING_GUILD(1592,3476,0,true),
    WOODCUTTING_GUILD_DUNGEON(1551,9872,0,true), YANILLE(2612,3092,0,true),
    ZANARIS(2384,4458,0,true), SULFUR_MINE(1454,3858,0,true),
    HALLOWED_SEPULCHER(2397,5983,0,true), COOKS_GUILD(3147,3450,0,true),
    CAM_TORUM(1451,9568,1,true), DORGESH_KAAN(2702,5349,0,true),
    SOPHANEM_DUNGEON(2800,5167,0,true), QUETZACALLI_GORGE(1519,3229,0,true),
    DARKFROST(1526,3292,0,true), NEMUS_RETREAT(1388,3310,0,true);

    private static final Set<BankLocation> BLACKLIST =
        Collections.synchronizedSet(EnumSet.noneOf(BankLocation.class));
    private final Tile tile;
    private final boolean membersOnly;
    private final BankType bankType;

    BankLocation(int x, int y, int z, boolean membersOnly)
    { this(x, y, z, membersOnly, BankType.BOOTH); }
    BankLocation(int x, int y, int z, boolean membersOnly, BankType bankType)
    {
        tile = new Tile(x, y, z); this.membersOnly = membersOnly; this.bankType = bankType;
    }
    public static List<BankLocation> getSortedValidLocations(Entity entity)
    { return getSortedValidLocations(entity == null ? null : entity.getTile()); }
    public static List<BankLocation> getSortedValidLocations(Tile origin)
    {
        List<BankLocation> result = new ArrayList<>(getValidLocations());
        if (origin != null) result.sort(Comparator.comparingDouble(value -> value.tile.distance(origin)));
        return result;
    }
    public static List<BankLocation> getValidLocations()
    {
        List<BankLocation> result = new ArrayList<>();
        boolean members = false;
        try { members = Client.hasMembersAccess(); } catch (RuntimeException ignored) { }
        for (BankLocation location : values())
            if (!BLACKLIST.contains(location) && (!location.membersOnly || members)) result.add(location);
        return Collections.unmodifiableList(result);
    }
    public static BankLocation getNearestEuclidean(Tile origin, boolean membersOnly)
    {
        BankLocation nearest = null; double distance = Double.POSITIVE_INFINITY;
        for (BankLocation location : values())
        {
            if (BLACKLIST.contains(location) || membersOnly && !location.membersOnly) continue;
            double candidate = origin == null ? location.tile.distance() : location.tile.distance(origin);
            if (candidate < distance) { nearest = location; distance = candidate; }
        }
        return nearest;
    }
    public static BankLocation getNearest(Entity entity) { return getNearest(entity == null ? null : entity.getTile()); }
    public static BankLocation getNearest(Tile tile) { return getNearest(tile, false); }
    public static BankLocation getNearest(Tile tile, boolean membersOnly)
    {
        List<BankLocation> sorted = getSortedValidLocations(tile);
        for (BankLocation location : sorted) if (!membersOnly || location.membersOnly) return location;
        return null;
    }
    public static BankLocation getNearest() { return getNearest((Tile) null); }
    public static void resetCache() { }
    public static void clearOld() { }
    public static boolean blacklist(BankLocation value) { return value != null && BLACKLIST.add(value); }
    public static boolean unblacklist(BankLocation value) { return value != null && BLACKLIST.remove(value); }
    public static boolean isBlacklisted(BankLocation value) { return value != null && BLACKLIST.contains(value); }
    public static void clearBlacklistedLocations() { BLACKLIST.clear(); }
    public Condition getCondition() { return () -> !membersOnly || Client.hasMembersAccess(); }
    public Area getArea(int radius) { return tile.getArea(radius); }
    @Override public Tile getTile() { return tile; }
    public Tile getCenter() { return tile; }
    public BankType getBankType() { return bankType; }
    public boolean isMembersOnly() { return membersOnly; }
    @Override public String toString()
    {
        String text = name().replace('_', ' ').toLowerCase(java.util.Locale.ENGLISH);
        StringBuilder result = new StringBuilder(text.length()); boolean upper = true;
        for (char c : text.toCharArray()) { result.append(upper ? Character.toUpperCase(c) : c); upper = c == ' '; }
        return result.toString();
    }
}
