package com.dreambotreborn.api.methods.container.impl.bank;

public enum BankScroll
{
    CLICK(0), WHEEL(1);

    private final int id;
    BankScroll(int id) { this.id = id; }
    public int getId() { return id; }
}
