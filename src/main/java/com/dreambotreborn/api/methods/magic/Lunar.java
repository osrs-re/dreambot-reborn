package com.dreambotreborn.api.methods.magic;

import com.dreambotreborn.api.methods.magic.cost.Rune;

public enum Lunar implements Spell
{
    LUNAR_HOME_TELEPORT(106,0,""), BAKE_PIE(107,65,"Astral:1,Fire:5,Water:4"),
    CURE_PLANT(108,66,"Astral:1,Earth:8"), MONSTER_EXAMINE(109,66,"Cosmic:1,Astral:1,Mind:1"),
    NPC_CONTACT(110,67,"Cosmic:1,Astral:1,Air:2"), CURE_OTHER(111,68,"Law:1,Astral:1,Earth:10"),
    HUMIDIFY(112,68,"Astral:1,Fire:1,Water:3"), MOONCLAN_TELEPORT(113,69,"Law:1,Astral:2,Earth:2"),
    TELE_GROUP_MOONCLAN(114,70,"Law:1,Astral:2,Earth:4"), CURE_ME(115,71,"Law:1,Cosmic:2,Astral:2"),
    HUNTER_KIT(116,71,"Astral:2,Earth:3"), WATERBIRTH_TELEPORT(117,72,"Law:1,Astral:2,Water:1"),
    TELE_GROUP_WATERBIRTH(118,73,"Law:1,Astral:2,Water:5"), CURE_GROUP(119,74,"Law:2,Cosmic:2,Astral:2"),
    STAT_SPY(120,75,"Cosmic:2,Astral:2,Body:5"), BARBARIAN_TELEPORT(121,75,"Law:2,Astral:2,Fire:3"),
    TELE_GROUP_BARBARIAN(122,76,"Law:2,Astral:2,Fire:6"), SUPERGLASS_MAKE(123,77,"Astral:2,Fire:6,Air:10"),
    TAN_LEATHER(124,78,"Nature:1,Astral:2,Fire:5"), KHAZARD_TELEPORT(125,78,"Law:2,Astral:2,Water:4"),
    TELE_GROUP_KHAZARD(126,79,"Law:2,Astral:2,Water:8"), DREAM(127,79,"Cosmic:1,Astral:2,Body:5"),
    STRING_JEWELLERY(128,80,"Astral:2,Earth:10,Water:5"), STAT_RESTORE_POTION_SHARE(129,81,"Astral:2,Earth:10,Water:10"),
    MAGIC_IMBUE(130,82,"Astral:2,Fire:7,Water:7"), FERTILE_SOIL(131,83,"Nature:2,Astral:3,Earth:15"),
    BOOST_POTION_SHARE(132,84,"Astral:3,Earth:12,Water:10"), FISHING_GUILD_TELEPORT(133,85,"Law:3,Astral:3,Water:10"),
    TELE_GROUP_FISHING_GUILD(134,86,"Law:3,Astral:3,Water:14"), PLANK_MAKE(135,86,"Nature:1,Astral:2,Earth:15"),
    CATHERBY_TELEPORT(136,87,"Law:3,Astral:3,Water:10"), TELE_GROUP_CATHERBY(137,88,"Law:3,Astral:3,Water:15"),
    RECHARGE_DRAGONSTONE(138,89,"Soul:1,Astral:1,Water:4"), ICE_PLATEAU_TELEPORT(139,89,"Law:3,Astral:3,Water:8"),
    TELE_GROUP_ICE_PLATEAU(140,90,"Law:3,Astral:3,Water:16"), ENERGY_TRANSFER(141,91,"Law:2,Nature:1,Astral:3"),
    HEAL_OTHER(142,92,"Blood:1,Law:3,Astral:3"), VENGEANCE_OTHER(143,93,"Death:2,Astral:3,Earth:10"),
    VENGEANCE(144,94,"Death:2,Astral:4,Earth:10"), HEAL_GROUP(145,95,"Blood:3,Law:6,Astral:4"),
    SPELLBOOK_SWAP(146,96,"Law:1,Cosmic:2,Astral:3"), GEOMANCY(147,65,"Nature:3,Astral:3,Earth:8"),
    SPIN_FLAX(148,76,"Nature:2,Astral:1,Air:5"), OURANIA_TELEPORT(149,71,"Law:1,Astral:2,Earth:6");
    private final int child,level; private final String cost;
    Lunar(int child,int level,String cost){this.child=child;this.level=level;this.cost=cost;}
    public double getExperience(){return SpellData.lunarExperience(this);} public int getLevel(){return level;}
    public int getParent(){return 218;} public int getChild(){return child;}
    public Rune[] getCost(){return SpellData.costs(cost);} public int getMaxHit(){return 0;}
    @Override public String toString(){return SpellData.title(this);}
}
