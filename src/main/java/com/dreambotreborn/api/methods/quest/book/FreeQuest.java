package com.dreambotreborn.api.methods.quest.book;

public enum FreeQuest implements Quest
{
    BELOW_ICE_MOUNTAIN(1), BLACK_KNIGHTS_FORTRESS(3), COOKS_ASSISTANT(1),
    CORSAIR_CURSE(2), THE_CORSAIR_CURSE(2), DEMON_SLAYER(3), DORICS_QUEST(1),
    DRAGON_SLAYER(2), DRAGON_SLAYER_I(2), ERNEST_THE_CHICKEN(4),
    GOBLIN_DIPLOMACY(5), IMP_CATCHER(1), THE_KNIGHTS_SWORD(1),
    MISTHALIN_MYSTERY(1), PIRATES_TREASURE(2), PRINCE_ALI_RESCUE(3),
    THE_RESTLESS_GHOST(1), ROMEO_AND_JULIET(5), RUNE_MYSTERIES(1),
    SHEEP_SHEARER(1), SHIELD_OF_ARRAV(1), VAMPIRE_SLAYER(3),
    VAMPYRE_SLAYER(3), WITCHS_POTION(1), X_MARKS_THE_SPOT(1), TUTORIAL_ISLAND(0);

    private final int questPoints;
    FreeQuest(int questPoints) { this.questPoints = questPoints; }
    @Override public int getQP() { return questPoints; }
}
