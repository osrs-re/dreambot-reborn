package com.dreambotreborn.api.methods.container.impl.bank;

import java.util.Locale;

/** Rearrangement and withdrawal display modes used by the bank interface. */
public enum BankMode
{
    SWAP(19), INSERT(21), ITEM(24), NOTE(26);

    private final int childId;

    BankMode(int childId) { this.childId = childId; }
    public int getChildId() { return childId; }

    @Override
    public String toString()
    {
        String value = name().replace('_', ' ');
        return value.substring(0, 1) + value.substring(1).toLowerCase(Locale.ENGLISH);
    }
}
