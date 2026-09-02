package com.dreambotreborn.api.methods.prayer;

import java.util.Locale;
import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.methods.skills.Skills;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public enum Prayer
{
    THICK_SKIN(1), BURST_OF_STRENGTH(4), CLARITY_OF_THOUGHT(7), SHARP_EYE(8),
    MYSTIC_WILL(9), ROCK_SKIN(10), SUPERHUMAN_STRENGTH(13), IMPROVED_REFLEXES(16),
    RAPID_RESTORE(19), RAPID_HEAL(22), PROTECT_ITEM(25), HAWK_EYE(26),
    MYSTIC_LORE(27), STEEL_SKIN(28), ULTIMATE_STRENGTH(31), INCREDIBLE_REFLEXES(34),
    PROTECT_FROM_MAGIC(37), PROTECT_FROM_MISSILES(40), PROTECT_FROM_MELEE(43),
    EAGLE_EYE(44), MYSTIC_MIGHT(45), RETRIBUTION(46), REDEMPTION(49), SMITE(52),
    PRESERVE(55), CHIVALRY(60), DEADEYE(62), MYSTIC_VIGOUR(63), PIETY(70),
    RIGOUR(74), AUGURY(77);

    private static final int PARENT = 541;
    private final int level;
    Prayer(int level) { this.level = level; }
    public int getLevel() { return level; }
    public int getSetting() { return unwrap().getVarbit(); }
    public int getChildIndex() { return ordinal() + 9; }
    public int getWidgetIndex() { return getChildIndex(); }
    public int getQuickPrayerChild() { return ordinal(); }
    public WidgetChild getWidgetChild()
    {
        net.runelite.api.widgets.Widget widget = com.dreambotreborn.api.DreamBotRebornApi.requireClient()
            .getWidget(PARENT, getChildIndex());
        return widget == null ? null : new WidgetChild(widget);
    }
    public boolean isUnlocked() { return Skills.getRealLevel(Skill.PRAYER) >= level; }
    net.runelite.api.Prayer unwrap() { return net.runelite.api.Prayer.valueOf(name()); }
    public static Prayer of(String name)
    {
        if (name == null) return null;
        String normalized = name.trim().replace(' ', '_').toUpperCase(Locale.ENGLISH);
        try { return valueOf(normalized); } catch (IllegalArgumentException ignored) { return null; }
    }
    @Override public String toString()
    {
        String value = name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
