package com.dreambotreborn.accounts;

/** Non-secret account information safe to display in Swing. */
public final class AccountSummary
{
    public final String id;
    public final String displayName;
    public final String username;

    AccountSummary(String id, String displayName, String username)
    {
        this.id = id;
        this.displayName = displayName;
        this.username = username;
    }

    @Override
    public String toString()
    {
        return displayName + " — " + username;
    }
}
