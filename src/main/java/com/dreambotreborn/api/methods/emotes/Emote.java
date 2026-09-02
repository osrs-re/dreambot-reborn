package com.dreambotreborn.api.methods.emotes;

public enum Emote
{
    AIR_GUITAR, ANGRY, BECKON, BLOW_KISS, BOW, CHEER, CLAP, CLIMB_ROPE,
    CRAZY_DANCE, CRY, DANCE, EXPLORE, FORTIS_SALUTE, FLAP, GLASS_BOX,
    GLASS_WALL, GOBLIN_BOW, GOBLIN_SALUTE, HEADBANG, HYPERMOBILE_DRINKER,
    IDEA, JIG, JOG, JUMP_FOR_JOY, LAUGH, LEAN, NO, PANIC, PARTY,
    PREMIER_SHIELD, PUSH_UP, RABBIT_HOP, RASPBERRY, SALUTE, SCARED, SHRUG,
    SIT_DOWN, SIT_UP, SKILL_CAPE, SLAP_HEAD, SMOOTH_DANCE, SPIN, STAMP,
    STAR_JUMP, THINK, TRICK, URI_TRANSFORM, WAVE, YAWN, YES, ZOMBIE_DANCE,
    ZOMBIE_HAND, ZOMBIE_WALK, FLEX;

    public final int childwidget;
    Emote() { childwidget = ordinal(); }
    public static Emote forChildWidgetId(int id)
    {
        for (Emote value : values()) if (value.childwidget == id) return value;
        return null;
    }
}
