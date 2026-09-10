package com.dreambotreborn.api.data;

public enum ClientLayout
{
    FIXED_CLASSIC("Fixed - Classic layout"),
    RESIZABLE_CLASSIC("Resizable - Classic layout"),
    RESIZABLE_MODERN("Resizable - Modern layout");

    private final String label;

    ClientLayout(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
