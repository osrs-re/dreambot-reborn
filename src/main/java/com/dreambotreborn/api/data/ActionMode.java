package com.dreambotreborn.api.data;

/** Player/NPC attack-option modes used by the in-game settings. */
public enum ActionMode
{
    DEPENDS_ON_COMBAT_LEVELS(0, "Depends on combat levels"),
    ALWAYS_RIGHT_CLICK(1, "Always right-click"),
    LEFT_CLICK_WHERE_AVAILABLE(2, "Left-click where available"),
    HIDDEN(3, "Hidden"),
    RIGHT_CLICK_FOR_CLANMATES(4, "Right-click for clanmates");

    private final int varbitValue;
    private final String option;

    ActionMode(int varbitValue, String option)
    {
        this.varbitValue = varbitValue;
        this.option = option;
    }

    public int getVarbitValue()
    {
        return varbitValue;
    }

    public String getOption()
    {
        return option;
    }

    public static ActionMode fromVarbitValue(int value)
    {
        for (ActionMode mode : values())
        {
            if (mode.varbitValue == value) return mode;
        }
        return DEPENDS_ON_COMBAT_LEVELS;
    }
}
