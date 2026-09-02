package com.dreambotreborn.api.methods.container.impl.bank;

import java.util.Locale;
import net.runelite.api.Varbits;
import com.dreambotreborn.api.DreamBotRebornApi;

public enum BankQuantitySelection
{
    ONE(30), FIVE(32), TEN(34), X(36), ALL(38);

    private final int childId;
    BankQuantitySelection(int childId) { this.childId = childId; }
    public int getChildId() { return childId; }

    public static BankQuantitySelection getSelection()
    {
        int value = DreamBotRebornApi.requireClient().getVarbitValue(Varbits.BANK_QUANTITY_TYPE);
        return value >= 0 && value < values().length ? values()[value] : null;
    }

    @Override
    public String toString()
    {
        String value = name().replace('_', ' ');
        return value.substring(0, 1) + value.substring(1).toLowerCase(Locale.ENGLISH);
    }
}
