package com.dreambotreborn.settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClientSettingsTest
{
    @Test
    void createsPersistsAndReloadsInputProfile(@TempDir Path directory) throws Exception
    {
        Path file = directory.resolve("settings.json");
        ClientSettings settings = ClientSettings.loadOrCreate(file);
        settings.setPreferredWorld(330);
        settings.setMouseSpeed(1250.0);
        settings.setMouseOvershoot(false);
        settings.setClickHoldMillis(72);
        settings.setKeyboardWpm(88.0);
        settings.setKeyboardMistakes(false);
        settings.setMouseTrailMillis(950L);

        ClientSettings reloaded = ClientSettings.loadOrCreate(file);
        assertEquals(330, reloaded.getPreferredWorld());
        assertEquals(1250.0, reloaded.getMouseSpeed());
        assertFalse(reloaded.isMouseOvershoot());
        assertEquals(72, reloaded.getClickHoldMillis());
        assertEquals(88.0, reloaded.getKeyboardWpm());
        assertEquals(950L, reloaded.getMouseTrailMillis());
    }

    @Test
    void rejectsUnsafePersistedProfileValues(@TempDir Path directory) throws Exception
    {
        Path file = directory.resolve("settings.json");
        Files.writeString(file,
            "{\"preferredWorld\": 301, \"mouseSpeed\": 99999}",
            StandardCharsets.UTF_8);
        assertThrows(IOException.class, () -> ClientSettings.loadOrCreate(file));
    }
}
