package com.dreambotreborn.api.methods.magic;

import com.dreambotreborn.api.methods.magic.cost.Rune;

public enum Arceuus implements Spell
{
    ARCEUS_HOME_TELEPORT(150,1,0,""), ARCEUUS_LIBRARY_TELEPORT(152,6,0,"Law:1,Earth:2"),
    BASIC_REANIMATION(151,16,0,"Nature:2,Body:4"), DRAYNOR_MANOR_TELEPORT(156,17,0,"Law:1,Earth:1,Water:1"),
    BATTLEFRONT_TELEPORT(168,23,0,"Law:1,Earth:1,Fire:1"), MIND_ALTAR_TELEPORT(158,28,0,"Law:1,Mind:2"),
    RESPAWN_TELEPORT(159,34,0,"Soul:1,Law:1"), GHOSTLY_GRASP(173,35,12,"Chaos:1,Air:4"),
    RESURRECT_LESSER_GHOST(186,38,0,"Mind:5,Air:10,Cosmic:1"), RESURRECT_LESSER_SKELETON(187,38,0,"Mind:5,Air:10,Cosmic:1"),
    RESURRECT_LESSER_ZOMBIE(188,38,0,"Mind:5,Air:10,Cosmic:1"), SALVE_GRAVEYARD_TELEPORT(160,40,0,"Soul:2,Law:1"),
    ADEPT_REANIMATION(153,41,0,"Soul:1,Nature:3,Body:4"), INFERIOR_DEMONBANE(169,44,16,"Fire:4,Chaos:1"),
    FENKENSTRAINS_CASTLE_TELEPORT(161,48,0,"Soul:1,Law:1,Earth:1"), SHADOW_VEIL(182,47,0,"Earth:5,Fire:5,Cosmic:5"),
    DARK_LURE(184,50,0,"Death:1,Nature:1"), SKELETAL_GRASP(174,56,17,"Death:1,Earth:8"),
    RESURRECT_SUPERIOR_GHOST(189,57,0,"Death:5,Earth:10,Cosmic:1"), RESURRECT_SUPERIOR_SKELETON(190,57,0,"Death:5,Earth:10,Cosmic:1"),
    RESURRECT_SUPERIOR_ZOMBIE(191,57,0,"Death:5,Earth:10,Cosmic:1"), MARK_OF_DARKNESS(172,59,0,"Soul:1,Cosmic:1"),
    WEST_ARDOUGNE_TELEPORT(162,61,0,"Soul:2,Law:2"), SUPERIOR_DEMONBANE(170,62,36,"Soul:1,Fire:8"),
    LESSER_CORRUPTION(177,64,0,"Death:1,Soul:2"), HARMONY_ISLAND_TELEPORT(163,65,0,"Soul:1,Law:1,Nature:1"),
    VILE_VIGOUR(183,66,0,"Soul:1,Air:3"), DEGRIME(181,70,0,"Earth:4,Nature:2"),
    CEMETERY_TELEPORT(164,71,0,"Soul:1,Blood:1,Law:1"), EXPERT_REANIMATION(154,72,0,"Blood:1,Nature:3,Soul:2"),
    WARD_OF_ARCEUUS(176,73,0,"Soul:4,Nature:2,Cosmic:1"), RESURRECT_GREATER_GHOST(192,76,0,"Blood:5,Fire:10,Cosmic:1"),
    RESURRECT_GREATER_SKELETON(193,76,0,"Blood:5,Fire:10,Cosmic:1"), RESURRECT_GREATER_ZOMBIE(194,76,0,"Blood:5,Fire:10,Cosmic:1"),
    RESURRECT_CROPS(165,78,0,"Soul:8,Blood:8,Nature:12,Earth:25"), UNDEAD_GRASP(175,79,24,"Blood:1,Fire:12"),
    DEATH_CHARGE(185,80,0,"Death:1,Blood:1,Soul:1"), DARK_DEMONBANE(171,82,30,"Soul:2,Fire:12"),
    BARROWS_TELEPORT(166,83,0,"Soul:2,Blood:1,Law:2"), DEMONIC_OFFERING(179,84,0,"Soul:1,Wrath:1"),
    GREATER_CORRUPTION(178,85,0,"Blood:1,Soul:3"), MASTER_REANIMATION(155,90,0,"Blood:2,Nature:4,Soul:4"),
    APE_ATOLL_TELEPORT(167,90,0,"Soul:2,Blood:2,Law:2"), SINISTER_OFFERING(180,92,0,"Blood:1,Wrath:1");
    private final int child,level,maxHit; private final String cost;
    Arceuus(int child,int level,int maxHit,String cost)
    {this.child=child;this.level=level;this.maxHit=maxHit;this.cost=cost;}
    public int getParent(){return 218;} public int getChild(){return child;} public int getLevel(){return level;}
    public double getMagicExp(){return SpellData.arceuusMagicExperience(this);}
    public double getPrayerExp(){return SpellData.arceuusPrayerExperience(this);}
    public Rune[] getCost(){return SpellData.costs(cost);} public int getMaxHit(){return maxHit;}
    public int getAutocastId(){return ordinal();} @Override public String toString(){return SpellData.title(this);}
}
