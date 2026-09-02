package com.dreambotreborn.api.methods.container.impl.bank;

import net.runelite.api.Varbits;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.wrappers.widgets.Widget;

public enum BankTab
{
    MAIN_TAB(0, -1), FIRST_TAB(1, Varbits.BANK_TAB_ONE_COUNT),
    SECOND_TAB(2, Varbits.BANK_TAB_TWO_COUNT), THIRD_TAB(3, Varbits.BANK_TAB_THREE_COUNT),
    FOURTH_TAB(4, Varbits.BANK_TAB_FOUR_COUNT), FIFTH_TAB(5, Varbits.BANK_TAB_FIVE_COUNT),
    SIXTH_TAB(6, Varbits.BANK_TAB_SIX_COUNT), SEVENTH_TAB(7, Varbits.BANK_TAB_SEVEN_COUNT),
    EIGHTH_TAB(8, Varbits.BANK_TAB_EIGHT_COUNT), NINTH_TAB(9, Varbits.BANK_TAB_NINE_COUNT);

    private final int id;
    private final int countVarbit;

    BankTab(int id, int countVarbit)
    {
        this.id = id;
        this.countVarbit = countVarbit;
    }

    public int getId() { return id; }
    public int getID() { return id; }
    public int getChild() { return id; }
    public int getConfig() { return countVarbit; }
    public Widget getWidgetChild() { return Bank.getTabWidget(this); }
    public boolean isOpen() { return Bank.getCurrentTab() == id; }
    public int count()
    {
        return countVarbit < 0 ? Bank.count(this)
            : DreamBotRebornApi.requireClient().getVarbitValue(countVarbit);
    }
    public boolean open() { return Bank.openTab(this); }
    public static BankTab getOpen() { return Bank.getCurrentBankTab(); }
}
