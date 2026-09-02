package com.dreambotreborn.accounts;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountLoginTest
{
    @Test
    void confirmsTheSelectedAccountInsteadOfAnyLoggedInAccount() throws Exception
    {
        AccountLogin.initialize(client(GameState.LOGGED_IN, "selected@example.com"));
        AccountCredentials credentials = new AccountCredentials(
            "SELECTED@example.com", "secret".toCharArray());

        java.util.concurrent.CompletableFuture<AccountLoginResult> result =
            AccountLogin.ensureLoggedIn(credentials);
        AccountLogin.tick();

        assertTrue(result.get(1, TimeUnit.SECONDS).isSuccessful());
        assertTrue(allCleared(credentials.password));
    }

    @Test
    void refusesToTreatADifferentLoggedInAccountAsTheSelection() throws Exception
    {
        AccountLogin.initialize(client(GameState.LOGGED_IN, "other@example.com"));
        AccountCredentials credentials = new AccountCredentials(
            "selected@example.com", "secret".toCharArray());

        java.util.concurrent.CompletableFuture<AccountLoginResult> result =
            AccountLogin.ensureLoggedIn(credentials);
        AccountLogin.tick();

        AccountLoginResult value = result.get(1, TimeUnit.SECONDS);
        assertFalse(value.isSuccessful());
        assertTrue(value.getMessage().contains("logout control"));
        assertTrue(allCleared(credentials.password));
    }

    private static Client client(GameState state, String username)
    {
        return (Client) Proxy.newProxyInstance(
            Client.class.getClassLoader(),
            new Class<?>[] {Client.class},
            (proxy, method, arguments) ->
            {
                if (method.getName().equals("getGameState"))
                {
                    return state;
                }
                if (method.getName().equals("getUsername"))
                {
                    return username;
                }
                if (method.getName().equals("toString"))
                {
                    return "AccountLoginTestClient";
                }
                Class<?> returnType = method.getReturnType();
                if (!returnType.isPrimitive())
                {
                    return null;
                }
                if (returnType == boolean.class)
                {
                    return false;
                }
                if (returnType == char.class)
                {
                    return '\0';
                }
                if (returnType == byte.class)
                {
                    return (byte) 0;
                }
                if (returnType == short.class)
                {
                    return (short) 0;
                }
                if (returnType == int.class)
                {
                    return 0;
                }
                if (returnType == long.class)
                {
                    return 0L;
                }
                if (returnType == float.class)
                {
                    return 0.0F;
                }
                return 0.0D;
            });
    }

    private static boolean allCleared(char[] value)
    {
        for (char character : value)
        {
            if (character != '\0')
            {
                return false;
            }
        }
        return true;
    }
}
