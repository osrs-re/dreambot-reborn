package com.dreambotreborn.api.methods.grandexchange;

public enum Status
{
    EMPTY(0),
    BUY(2),
    BUY_COLLECT(5),
    SELL(10),
    SELL_COLLECT(13);

    private final int statusValue;

    Status(int statusValue)
    {
        this.statusValue = statusValue;
    }

    public int getStatusValue()
    {
        return statusValue;
    }
}
