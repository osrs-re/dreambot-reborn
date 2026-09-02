package com.dreambotreborn.accounts;

/** Result of preparing the selected account for a script. */
public final class AccountLoginResult
{
    private final boolean successful;
    private final String message;

    private AccountLoginResult(boolean successful, String message)
    {
        this.successful = successful;
        this.message = message;
    }

    public static AccountLoginResult success(String message)
    {
        return new AccountLoginResult(true, message);
    }

    public static AccountLoginResult failure(String message)
    {
        return new AccountLoginResult(false, message);
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public String getMessage()
    {
        return message;
    }
}
