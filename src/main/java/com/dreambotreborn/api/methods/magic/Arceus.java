package com.dreambotreborn.api.methods.magic;

import com.dreambotreborn.api.methods.magic.cost.Rune;

/** Older Arceuus spellbook names retained for scripts written before its rework. */
public enum Arceus implements Spell
{
    ARCEUS_HOME_TELEPORT, REANIMATE_GOBLIN, ARCEUUS_LIBRARY_TELEPORT,
    LUMBRIDGE_GRAVEYARD_TELEPORT, REANIMATE_MONKEY, REANIMATE_IMP,
    REANIMATE_MINOTAUR, BASIC_REANIMATION, DRAYNOR_MANOR_TELEPORT,
    REANIMATE_SCORPTION, REANIMATE_BEAR, REANIMATE_UNICORN, REANIMATE_DOG,
    BATTLEFRONT_TELEPORT, MIND_ALTAR_TELEPORT, REANIMATE_CHAOS_DRUID,
    RESPAWN_TELEPORT, REANIMATE_GIANT, GHOSTLY_GRASP, RESURRECT_LESSER_GHOST,
    RESURRECT_LESSER_SKELETON, RESURRECT_LESSER_ZOMBIE, SALVE_GRAVEYARD_TELEPORT,
    REANIMATE_OGRE, REANIMATE_ELF, REANIMATE_TROLL, ADEPT_REANIMATION,
    FENKENSTRAINS_CASTLE_TELEPORT, REANIMATE_HORROR, REANIMATE_KALPHITE,
    SHADOW_VEIL, DARK_LURE, SKELETAL_GRASP, RESURRECT_SUPERIOR_GHOST,
    RESURRECT_SUPERIOR_SKELETON, RESURRECT_SUPERIOR_ZOMBIE, MARK_OF_DARKNESS,
    WEST_ARDOUGNE_TELEPORT, REANIMATE_DAGANNOTH, REANIMATE_BLOODVELD,
    SUPERIOR_DEMONBANE, LESSER_CORRUPTION, HARMONY_ISLAND_TELEPORT,
    REANIMATE_TZHAAR, VILE_VIGOUR, DEGRIME, CEMETERY_TELEPORT, REANIMATE_DEMON,
    REANIMATE_AVIANSIE, EXPERT_REANIMATION, WARD_OF_ARCEUUS,
    RESURRECT_GREATER_GHOST, RESURRECT_GREATER_SKELETON, RESURRECT_GREATER_ZOMBIE,
    RESURRECT_CROPS, UNDEAD_GRASP, DEATH_CHARGE, DARK_DEMONBANE, BARROWS_TELEPORT,
    REANIMATE_ABYSSAL_CREATURE, DEMONIC_OFFERING, GREATER_CORRUPTION,
    MASTER_REANIMATION, APE_ATOLL_TELEPORT, REANIMATE_DRAGON, SINISTER_OFFERING;

    private Arceuus current()
    {
        try { return Arceuus.valueOf(name()); }
        catch (IllegalArgumentException removedSpell) { return null; }
    }
    public int getParent(){return 218;}
    public int getChild(){Arceuus value=current(); return value == null ? -1 : value.getChild();}
    public int getLevel(){Arceuus value=current(); return value == null ? 0 : value.getLevel();}
    public Rune[] getCost(){Arceuus value=current(); return value == null ? new Rune[0] : value.getCost();}
    public double getMagicExp(){return 0.0;} public double getPrayerExp(){return 0.0;}
    public int getMaxHit(){Arceuus value=current(); return value == null ? 0 : value.getMaxHit();}
    @Override public String toString(){return SpellData.title(this);}
}
