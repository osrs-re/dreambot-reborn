package com.dreambotreborn.api.methods.hotkeys;

public enum NPCAttackOption
{
    LEFT_CLICK_AVAILABLE(2), DEPEND_ON_CB_LEVEL(0), ALWAYS_RIGHT(1), HIDDEN(3);
    private final int value;
    NPCAttackOption(int value) { this.value = value; }
    public int getConfig() { return 1306; }
    public int getVal() { return value; }
}
