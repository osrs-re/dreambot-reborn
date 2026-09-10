package com.dreambotreborn.api.wrappers.interactive.composite;

public class NameComposite
{
    private final String name;
    public NameComposite(Object reference)
    {
        this(reference instanceof net.runelite.api.Nameable
            ? ((net.runelite.api.Nameable) reference).getName() : String.valueOf(reference));
    }
    public NameComposite(String name) { this.name = name == null ? "" : name; }
    public String getName() { return name; }
    public String getFormatted()
    {
        return name.replaceAll("[ _-]", "").toLowerCase(java.util.Locale.ENGLISH);
    }
}
