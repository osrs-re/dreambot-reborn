package com.dreambotreborn.api.methods.container.impl.bank;

public enum BankType
{
    BOOTH("Bank"),
    CHEST("Use", "Bank"),
    NPC("Bank"),
    TABLE("Use"),
    EXCHANGE("Exchange"),
    BUFFALO("Bank");

    private final String[] actions;
    BankType(String... actions) { this.actions = actions; }
    public String[] getActions() { return actions.clone(); }
}
