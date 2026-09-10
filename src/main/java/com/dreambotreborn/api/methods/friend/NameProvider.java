package com.dreambotreborn.api.methods.friend;

import com.dreambotreborn.api.wrappers.interactive.composite.NameComposite;

public class NameProvider
{
    private final String name;
    private final String previousName;
    protected NameProvider(String name, String previousName)
    {
        this.name = name == null ? "" : name;
        this.previousName = previousName == null ? "" : previousName;
    }
    public NameComposite getNameComposite() { return new NameComposite(name); }
    public NameComposite getPreviousNameComposite() { return new NameComposite(previousName); }
    public String getName() { return name; }
    public String getPreviousName() { return previousName; }
    public String getOtherName() { return previousName; }
    public static String format(String value)
    {
        return value == null ? "" : value.replace(' ', ' ').trim();
    }
}
