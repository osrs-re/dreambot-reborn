package com.dreambotreborn.api.methods.tabs;

/** Side-panel tabs with their client tab index. */
public enum Tab
{
    COMBAT(0),
    SKILLS(1),
    QUEST(2),
    INVENTORY(3),
    EQUIPMENT(4),
    PRAYER(5),
    MAGIC(6),
    CLAN(7),
    ACCOUNT_MANAGEMENT(8),
    FRIENDS(9),
    LOGOUT(10),
    OPTIONS(11),
    EMOTES(12),
    MUSIC(13);

    private final int index;

    Tab(int index)
    {
        this.index = index;
    }

    public int getIndex()
    {
        return index;
    }

    public int getId()
    {
        return index;
    }

    public boolean isOpen()
    {
        return Tabs.isOpen(this);
    }

    public boolean open()
    {
        return Tabs.open(this);
    }

    public java.util.concurrent.CompletableFuture<Boolean> openAsync()
    {
        return Tabs.openAsync(this);
    }
}
