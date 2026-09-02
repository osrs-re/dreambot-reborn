package com.dreambotreborn.accounts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Simple plaintext JSON storage intended for local test accounts. */
final class AccountStore
{
    private static final int FORMAT_VERSION = 1;
    private static final int MAX_FILE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_ACCOUNTS = 1_000;

    private final Path path;
    private final List<Entry> entries = new ArrayList<>();
    private boolean loaded;

    AccountStore(Path path)
    {
        this.path = Objects.requireNonNull(path, "path");
    }

    synchronized void load() throws StorageException
    {
        if (loaded)
        {
            return;
        }
        if (!Files.exists(path))
        {
            loaded = true;
            return;
        }

        try
        {
            long size = Files.size(path);
            if (size <= 0 || size > MAX_FILE_BYTES)
            {
                throw new StorageException("accounts.json has an invalid size");
            }
            JSONObject root = new JSONObject(Files.readString(path, StandardCharsets.UTF_8));
            if (root.optInt("version", -1) != FORMAT_VERSION)
            {
                throw new StorageException("Unsupported accounts.json version");
            }
            JSONArray accounts = root.getJSONArray("accounts");
            if (accounts.length() > MAX_ACCOUNTS)
            {
                throw new StorageException("accounts.json contains too many accounts");
            }

            List<Entry> loadedEntries = new ArrayList<>(accounts.length());
            Set<String> ids = new HashSet<>();
            try
            {
                for (int index = 0; index < accounts.length(); index++)
                {
                    JSONObject account = accounts.getJSONObject(index);
                    String id = account.getString("id");
                    String displayName = account.getString("displayName");
                    String username = account.getString("username");
                    char[] password = account.getString("password").toCharArray();
                    try
                    {
                        validate(displayName, username, password, false);
                        if (id.trim().isEmpty() || !ids.add(id))
                        {
                            throw new StorageException("accounts.json contains an invalid account id");
                        }
                        loadedEntries.add(new Entry(id, displayName, username, password));
                    }
                    finally
                    {
                        Arrays.fill(password, '\0');
                    }
                }
            }
            catch (StorageException | JSONException ex)
            {
                destroy(loadedEntries);
                throw ex;
            }
            entries.addAll(loadedEntries);
            loaded = true;
        }
        catch (IOException | JSONException ex)
        {
            throw new StorageException("Unable to read accounts.json", ex);
        }
    }

    synchronized List<AccountSummary> summaries()
    {
        requireLoaded();
        List<AccountSummary> result = new ArrayList<>(entries.size());
        for (Entry entry : entries)
        {
            result.add(entry.summary());
        }
        return result;
    }

    synchronized AccountSummary add(
        String displayName, String username, char[] password) throws StorageException
    {
        requireLoaded();
        validate(displayName, username, password, false);
        List<Entry> updated = copies();
        Entry added = new Entry(
            UUID.randomUUID().toString(), displayName.trim(), username.trim(), password);
        updated.add(added);
        replaceAfterPersist(updated);
        return added.summary();
    }

    synchronized void update(
        String id, String displayName, String username, char[] replacementPassword)
        throws StorageException
    {
        requireLoaded();
        validate(displayName, username, replacementPassword, true);
        List<Entry> updated = copies();
        Entry existing = find(updated, id);
        if (existing == null)
        {
            destroy(updated);
            throw new StorageException("The selected account no longer exists");
        }
        char[] password = replacementPassword == null || replacementPassword.length == 0
            ? existing.password : replacementPassword;
        Entry replacement = new Entry(id, displayName.trim(), username.trim(), password);
        int index = updated.indexOf(existing);
        existing.destroy();
        updated.set(index, replacement);
        replaceAfterPersist(updated);
    }

    synchronized void remove(String id) throws StorageException
    {
        requireLoaded();
        List<Entry> updated = copies();
        Entry existing = find(updated, id);
        if (existing == null)
        {
            destroy(updated);
            throw new StorageException("The selected account no longer exists");
        }
        updated.remove(existing);
        existing.destroy();
        replaceAfterPersist(updated);
    }

    synchronized AccountCredentials credentials(String id) throws StorageException
    {
        requireLoaded();
        Entry entry = find(entries, id);
        if (entry == null)
        {
            throw new StorageException("The selected account no longer exists");
        }
        return new AccountCredentials(entry.username, entry.password);
    }

    private void replaceAfterPersist(List<Entry> updated) throws StorageException
    {
        try
        {
            persist(updated);
        }
        catch (StorageException ex)
        {
            destroy(updated);
            throw ex;
        }
        destroy(entries);
        entries.clear();
        entries.addAll(updated);
    }

    private void persist(List<Entry> values) throws StorageException
    {
        try
        {
            JSONArray accounts = new JSONArray();
            for (Entry entry : values)
            {
                JSONObject account = new JSONObject();
                account.put("id", entry.id);
                account.put("displayName", entry.displayName);
                account.put("username", entry.username);
                account.put("password", new String(entry.password));
                accounts.put(account);
            }
            JSONObject root = new JSONObject();
            root.put("version", FORMAT_VERSION);
            root.put("accounts", accounts);
            writeAtomically(root.toString(2) + System.lineSeparator());
        }
        catch (IOException | JSONException ex)
        {
            throw new StorageException("Unable to save accounts.json", ex);
        }
    }

    private void writeAtomically(String json) throws IOException
    {
        Path parent = path.getParent();
        if (parent != null)
        {
            Files.createDirectories(parent);
        }
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try
        {
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            setOwnerOnlyPermissions(temporary);
            try
            {
                Files.move(temporary, path,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException ignored)
            {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            setOwnerOnlyPermissions(path);
        }
        finally
        {
            Files.deleteIfExists(temporary);
        }
    }

    private static void setOwnerOnlyPermissions(Path value)
    {
        try
        {
            Files.setPosixFilePermissions(value, EnumSet.of(
                PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        }
        catch (IOException | UnsupportedOperationException ignored)
        {
            // Windows and non-POSIX filesystems use their regular user ACL.
        }
    }

    private List<Entry> copies()
    {
        List<Entry> result = new ArrayList<>(entries.size());
        for (Entry entry : entries)
        {
            result.add(entry.copy());
        }
        return result;
    }

    private static Entry find(List<Entry> values, String id)
    {
        for (Entry entry : values)
        {
            if (entry.id.equals(id))
            {
                return entry;
            }
        }
        return null;
    }

    private static void validate(
        String displayName, String username, char[] password, boolean passwordOptional)
        throws StorageException
    {
        if (displayName == null || displayName.trim().isEmpty())
        {
            throw new StorageException("Enter a display name");
        }
        if (username == null || username.trim().isEmpty())
        {
            throw new StorageException("Enter the account username or email");
        }
        if (!passwordOptional && (password == null || password.length == 0))
        {
            throw new StorageException("Enter the account password");
        }
        if (displayName.length() > 200 || username.length() > 320
            || password != null && password.length > 1024)
        {
            throw new StorageException("One of the account fields is too long");
        }
    }

    private void requireLoaded()
    {
        if (!loaded)
        {
            throw new IllegalStateException("Account storage has not been loaded");
        }
    }

    private static void destroy(List<Entry> values)
    {
        for (Entry entry : values)
        {
            entry.destroy();
        }
    }

    private static final class Entry
    {
        private final String id;
        private final String displayName;
        private final String username;
        private final char[] password;

        private Entry(String id, String displayName, String username, char[] password)
        {
            this.id = id;
            this.displayName = displayName;
            this.username = username;
            this.password = Arrays.copyOf(password, password.length);
        }

        private Entry copy()
        {
            return new Entry(id, displayName, username, password);
        }

        private AccountSummary summary()
        {
            return new AccountSummary(id, displayName, username);
        }

        private void destroy()
        {
            Arrays.fill(password, '\0');
        }
    }

    static final class StorageException extends Exception
    {
        private StorageException(String message)
        {
            super(message);
        }

        private StorageException(String message, Throwable cause)
        {
            super(message, cause);
        }
    }
}
