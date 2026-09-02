package com.dreambotreborn.accounts;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountStoreTest
{
    @Test
    void storesAndReloadsPlaintextAccountsWithoutUnlocking(@TempDir Path directory)
        throws Exception
    {
        Path file = directory.resolve("accounts.json");
        AccountStore store = new AccountStore(file);
        store.load();
        AccountSummary saved = store.add(
            "Test account", "test@example.com", "test-password".toCharArray());

        String json = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(json.contains("test@example.com"));
        assertTrue(json.contains("test-password"));

        AccountStore restarted = new AccountStore(file);
        restarted.load();
        assertEquals(1, restarted.summaries().size());
        assertEquals(saved.id, restarted.summaries().get(0).id);
        try (AccountCredentials credentials = restarted.credentials(saved.id))
        {
            assertEquals("test@example.com", credentials.username);
            assertEquals("test-password", new String(credentials.password));
        }
    }

    @Test
    void updatesAndRemovesAccounts(@TempDir Path directory) throws Exception
    {
        AccountStore store = new AccountStore(directory.resolve("accounts.json"));
        store.load();
        AccountSummary saved = store.add("One", "one@example.com", "first".toCharArray());

        store.update(saved.id, "Updated", "updated@example.com", new char[0]);
        assertEquals("Updated", store.summaries().get(0).displayName);
        try (AccountCredentials credentials = store.credentials(saved.id))
        {
            assertEquals("first", new String(credentials.password));
        }

        store.remove(saved.id);
        assertTrue(store.summaries().isEmpty());
    }
}
