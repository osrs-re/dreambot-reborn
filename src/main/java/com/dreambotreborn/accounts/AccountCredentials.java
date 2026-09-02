package com.dreambotreborn.accounts;

import java.util.Arrays;

/** Short-lived credential copy which can be explicitly erased. */
final class AccountCredentials implements AutoCloseable
{
    final String username;
    final char[] password;

    AccountCredentials(String username, char[] password)
    {
        this.username = username;
        this.password = Arrays.copyOf(password, password.length);
    }

    @Override
    public void close()
    {
        Arrays.fill(password, '\0');
    }
}
