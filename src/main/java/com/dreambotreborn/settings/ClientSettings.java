package com.dreambotreborn.settings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Small continuously persisted JSON settings model. */
public final class ClientSettings
{
    private static final Pattern PREFERRED_WORLD = Pattern.compile(
        "\\\"preferredWorld\\\"\\s*:\\s*(-?\\d+)");
    private static final Pattern MOUSE_SPEED = Pattern.compile(
        "\\\"mouseSpeed\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern MOUSE_OVERSHOOT = Pattern.compile(
        "\\\"mouseOvershoot\\\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CLICK_HOLD = Pattern.compile(
        "\\\"clickHoldMillis\\\"\\s*:\\s*(\\d+)");
    private static final Pattern KEYBOARD_WPM = Pattern.compile(
        "\\\"keyboardWpm\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern KEYBOARD_MISTAKES = Pattern.compile(
        "\\\"keyboardMistakes\\\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRAIL_DURATION = Pattern.compile(
        "\\\"mouseTrailMillis\\\"\\s*:\\s*(\\d+)");
    private static final int MIN_WORLD = 301;
    private static final int MAX_WORLD = 999;

    private final Path path;
    private volatile int preferredWorld;
    private volatile double mouseSpeed;
    private volatile boolean mouseOvershoot;
    private volatile int clickHoldMillis;
    private volatile double keyboardWpm;
    private volatile boolean keyboardMistakes;
    private volatile long mouseTrailMillis;

    private ClientSettings(
        Path path, int preferredWorld, double mouseSpeed, boolean mouseOvershoot,
        int clickHoldMillis, double keyboardWpm, boolean keyboardMistakes,
        long mouseTrailMillis)
    {
        this.path = path;
        this.preferredWorld = preferredWorld;
        this.mouseSpeed = mouseSpeed;
        this.mouseOvershoot = mouseOvershoot;
        this.clickHoldMillis = clickHoldMillis;
        this.keyboardWpm = keyboardWpm;
        this.keyboardMistakes = keyboardMistakes;
        this.mouseTrailMillis = mouseTrailMillis;
    }

    public static ClientSettings loadOrCreate(Path path) throws IOException
    {
        Objects.requireNonNull(path, "path");
        if (!Files.exists(path))
        {
            ClientSettings settings = defaults(path);
            settings.save();
            return settings;
        }

        String json = Files.readString(path, StandardCharsets.UTF_8);
        Matcher matcher = PREFERRED_WORLD.matcher(json);
        if (!matcher.find())
        {
            throw new IOException("settings.json has no numeric preferredWorld property");
        }
        int world;
        try
        {
            world = Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException ex)
        {
            throw new IOException("settings.json contains an invalid preferredWorld", ex);
        }
        double mouseSpeed = doubleValue(json, MOUSE_SPEED, 900.0);
        int clickHoldMillis = intValue(json, CLICK_HOLD, 55);
        double keyboardWpm = doubleValue(json, KEYBOARD_WPM, 105.0);
        long trailMillis = intValue(json, TRAIL_DURATION, 600);
        validateWorld(world);
        validateRange(mouseSpeed, 100.0, 2500.0, "Mouse speed");
        validateRange(clickHoldMillis, 1, 500, "Click hold");
        validateRange(keyboardWpm, 10.0, 300.0, "Keyboard speed");
        validateRange(trailMillis, 0, 10_000, "Mouse trail");
        return new ClientSettings(path, world,
            mouseSpeed,
            booleanValue(json, MOUSE_OVERSHOOT, true),
            clickHoldMillis,
            keyboardWpm,
            booleanValue(json, KEYBOARD_MISTAKES, false),
            trailMillis);
    }

    /** Defaults without overwriting an invalid existing file. */
    public static ClientSettings defaults(Path path)
    {
        return new ClientSettings(Objects.requireNonNull(path, "path"),
            0, 900.0, true, 55, 105.0, false, 600L);
    }

    public int getPreferredWorld()
    {
        return preferredWorld;
    }

    public Path getPath()
    {
        return path;
    }

    public double getMouseSpeed() { return mouseSpeed; }
    public boolean isMouseOvershoot() { return mouseOvershoot; }
    public int getClickHoldMillis() { return clickHoldMillis; }
    public double getKeyboardWpm() { return keyboardWpm; }
    public boolean isKeyboardMistakes() { return keyboardMistakes; }
    public long getMouseTrailMillis() { return mouseTrailMillis; }

    public synchronized void setMouseSpeed(double value) throws IOException
    {
        validateRange(value, 100.0, 2500.0, "Mouse speed");
        double previous = mouseSpeed;
        mouseSpeed = value;
        try { save(); }
        catch (IOException ex) { mouseSpeed = previous; throw ex; }
    }

    public synchronized void setMouseOvershoot(boolean value) throws IOException
    {
        boolean previous = mouseOvershoot;
        mouseOvershoot = value;
        try { save(); }
        catch (IOException ex) { mouseOvershoot = previous; throw ex; }
    }

    public synchronized void setClickHoldMillis(int value) throws IOException
    {
        validateRange(value, 1, 500, "Click hold");
        int previous = clickHoldMillis;
        clickHoldMillis = value;
        try { save(); }
        catch (IOException ex) { clickHoldMillis = previous; throw ex; }
    }

    public synchronized void setKeyboardWpm(double value) throws IOException
    {
        validateRange(value, 10.0, 300.0, "Keyboard speed");
        double previous = keyboardWpm;
        keyboardWpm = value;
        try { save(); }
        catch (IOException ex) { keyboardWpm = previous; throw ex; }
    }

    public synchronized void setKeyboardMistakes(boolean value) throws IOException
    {
        boolean previous = keyboardMistakes;
        keyboardMistakes = value;
        try { save(); }
        catch (IOException ex) { keyboardMistakes = previous; throw ex; }
    }

    public synchronized void setMouseTrailMillis(long value) throws IOException
    {
        validateRange(value, 0, 10_000, "Mouse trail");
        long previous = mouseTrailMillis;
        mouseTrailMillis = value;
        try { save(); }
        catch (IOException ex) { mouseTrailMillis = previous; throw ex; }
    }

    /** A value of zero disables preferred-world selection. */
    public synchronized void setPreferredWorld(int world) throws IOException
    {
        validateWorld(world);
        int previous = preferredWorld;
        preferredWorld = world;
        try
        {
            save();
        }
        catch (IOException ex)
        {
            preferredWorld = previous;
            throw ex;
        }
    }

    private synchronized void save() throws IOException
    {
        String json = "{\n"
            + "  \"version\": 2,\n"
            + "  \"preferredWorld\": " + preferredWorld + ",\n"
            + "  \"mouseSpeed\": " + mouseSpeed + ",\n"
            + "  \"mouseOvershoot\": " + mouseOvershoot + ",\n"
            + "  \"clickHoldMillis\": " + clickHoldMillis + ",\n"
            + "  \"keyboardWpm\": " + keyboardWpm + ",\n"
            + "  \"keyboardMistakes\": " + keyboardMistakes + ",\n"
            + "  \"mouseTrailMillis\": " + mouseTrailMillis + "\n"
            + "}\n";
        Path parent = path.getParent();
        if (parent != null)
        {
            Files.createDirectories(parent);
        }
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try
        {
            Files.writeString(temporary, json, StandardCharsets.UTF_8);
            try
            {
                Files.move(temporary, path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
            }
            catch (AtomicMoveNotSupportedException ignored)
            {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally
        {
            Files.deleteIfExists(temporary);
        }
    }

    private static void validateWorld(int world) throws IOException
    {
        if (world != 0 && (world < MIN_WORLD || world > MAX_WORLD))
        {
            throw new IOException("Preferred world must be between "
                + MIN_WORLD + " and " + MAX_WORLD + ", or disabled");
        }
    }

    private static void validateRange(double value, double minimum, double maximum, String label)
        throws IOException
    {
        if (!Double.isFinite(value) || value < minimum || value > maximum)
        {
            throw new IOException(label + " must be between " + minimum + " and " + maximum);
        }
    }

    private static int intValue(String json, Pattern pattern, int fallback)
    {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) return fallback;
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static double doubleValue(String json, Pattern pattern, double fallback)
    {
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) return fallback;
        try { return Double.parseDouble(matcher.group(1)); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static boolean booleanValue(String json, Pattern pattern, boolean fallback)
    {
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? Boolean.parseBoolean(matcher.group(1)) : fallback;
    }
}
