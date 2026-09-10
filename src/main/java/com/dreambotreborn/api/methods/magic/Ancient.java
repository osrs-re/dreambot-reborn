package com.dreambotreborn.api.methods.magic;

import com.dreambotreborn.api.methods.magic.cost.Rune;

public enum Ancient implements Spell
{
    HOME_TELEPORT(105,0,0,""), ICE_RUSH(81,58,17,"Death:2,Chaos:2,Water:2"),
    ICE_BLITZ(82,82,26,"Blood:2,Death:2,Water:3"), ICE_BURST(83,70,22,"Death:2,Chaos:4,Water:4"),
    ICE_BARRAGE(84,94,30,"Blood:2,Death:4,Water:6"), BLOOD_RUSH(85,56,16,"Blood:1,Death:2,Chaos:2"),
    BLOOD_BLITZ(86,80,25,"Blood:4,Death:2"), BLOOD_BURST(87,68,21,"Blood:2,Death:2,Chaos:4"),
    BLOOD_BARRAGE(88,92,29,"Soul:1,Blood:4,Death:4"), SMOKE_RUSH(89,50,14,"Death:2,Chaos:2,Fire:1,Air:4"),
    SMOKE_BLITZ(90,74,23,"Blood:2,Death:2,Fire:2,Air:2"), SMOKE_BURST(91,62,18,"Death:2,Chaos:4,Fire:2,Air:2"),
    SMOKE_BARRAGE(92,86,27,"Blood:2,Death:4,Fire:4,Air:4"), SHADOW_RUSH(93,52,15,"Soul:1,Death:2,Chaos:2,Air:1"),
    SHADOW_BLITZ(94,76,24,"Soul:2,Blood:2,Death:2,Air:2"), SHADOW_BURST(95,64,19,"Soul:2,Death:2,Chaos:4,Air:1"),
    SHADOW_BARRAGE(96,88,28,"Soul:3,Blood:2,Death:4,Air:4"), PADDEWWA_TELEPORT(97,54,0,"Law:2,Fire:1,Air:1"),
    SENNTISTEN_TELEPORT(98,60,0,"Soul:1,Law:2"), KHARYRLL_TELEPORT(99,66,0,"Blood:1,Law:2"),
    LASSAR_TELEPORT(100,72,0,"Law:2,Water:4"), DAREEYAK_TELEPORT(101,78,0,"Law:2,Fire:3,Air:2"),
    CARRALLANGER_TELEPORT(102,84,0,"Soul:2,Law:2"), ANNAKARL_TELEPORT(103,90,0,"Blood:2,Law:2"),
    GHORROCK_TELEPORT(104,96,0,"Law:2,Water:8"), PADEWWA_TELEPORT(97,54,0,"Law:2,Fire:1,Air:1"),
    CARRALLANGAR_TELEPORT(102,84,0,"Soul:2,Law:2");
    private final int child, level, maxHit; private final String cost;
    Ancient(int child,int level,int maxHit,String cost)
    { this.child=child;this.level=level;this.maxHit=maxHit;this.cost=cost; }
    public int getParent(){return 218;} public int getChild(){return child;} public int getLevel(){return level;}
    public int getAutocastId(){return ordinal();} public Rune[] getCost(){return SpellData.costs(cost);}
    public double getExp(){return SpellData.ancientExperience(this);} public int getMaxHit(){return maxHit;}
    @Override public String toString(){return SpellData.title(this);}
}
