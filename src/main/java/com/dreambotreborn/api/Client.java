package com.dreambotreborn.api;

import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.runelite.api.WorldType;
import net.runelite.api.vars.AccountType;
import net.runelite.api.coords.WorldPoint;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.data.GameState;
import com.dreambotreborn.api.methods.map.Tile;

/** Frequently used client state with DreamBot-style static access. */
public final class Client
{
    private static volatile int forcedFps;
    private static volatile boolean renderingDisabled;
    private static volatile boolean cameraInMotion;
    private Client()
    {
    }

    public static boolean isLoggedIn()
    {
        return getGameState() == GameState.LOGGED_IN;
    }

    public static GameState getGameState()
    {
        return GameState.fromRuneLite(getRuneLiteGameState());
    }

    /** Access to the raw RuneLite state for integrations that need it. */
    public static net.runelite.api.GameState getRuneLiteGameState()
    {
        return DreamBotRebornApi.requireClient().getGameState();
    }

    public static int getGameStateId()
    {
        GameState state = getGameState();
        return state == null ? -1 : state.getId();
    }

    public static int getGameStateID()
    {
        return getGameStateId();
    }

    public static int getGameCycle()
    {
        return DreamBotRebornApi.requireClient().getGameCycle();
    }

    public static int getGameTick()
    {
        return DreamBotRebornApi.requireClient().getTickCount();
    }

    public static int getFPS()
    {
        return DreamBotRebornApi.requireClient().getFPS();
    }

    public static int getIdleTime()
    {
        return DreamBotRebornApi.requireClient().getMouseIdleTicks();
    }

    public static void setIdleTime(int ticks)
    {
        if (ticks <= 0)
        {
            java.awt.Point point = com.dreambotreborn.api.input.Mouse.getPosition();
            com.dreambotreborn.api.input.Mouse.hop(point);
        }
    }

    public static boolean readyToLoad()
    {
        try { return DreamBotRebornApi.requireClient() != null; }
        catch (IllegalStateException ignored) { return false; }
    }

    public static int getForcedFPS() { return forcedFps; }
    public static void setForcedFPS(int value) { forcedFps = Math.max(0, value); }
    public static boolean isRenderingDisabled() { return renderingDisabled; }
    public static void setRenderingDisabled(boolean value) { renderingDisabled = value; }

    public static int getLoginIndex()
    {
        return DreamBotRebornApi.requireClient().getLoginIndex();
    }

    public static int getPlane()
    {
        return DreamBotRebornApi.requireClient().getPlane();
    }

    public static int getBaseX()
    {
        return DreamBotRebornApi.requireClient().getBaseX();
    }

    public static int getBaseY()
    {
        return DreamBotRebornApi.requireClient().getBaseY();
    }

    public static Tile getBase()
    {
        return new Tile(getBaseX(), getBaseY(), getPlane());
    }

    public static int getMapAngle() { return DreamBotRebornApi.requireClient().getCameraYawTarget(); }
    public static void setMapAngleDirect(int angle)
    {
        DreamBotRebornApi.requireClient().setCameraYawTarget(angle & 2047);
    }

    public static boolean isWorldSelectorOpen()
    {
        return com.dreambotreborn.api.methods.widget.Widgets.visible().stream().anyMatch(widget ->
            (widget.text + widget.name).toLowerCase().contains("world select"));
    }

    public static boolean isInCutscene()
    {
        return getGameState() == GameState.LOGGED_IN
            && DreamBotRebornApi.requireClient().getLocalPlayer() == null;
    }

    public static byte[][][] getTileSettings() { return DreamBotRebornApi.requireClient().getTileSettings(); }
    public static int[][][] getTileHeights() { return DreamBotRebornApi.requireClient().getTileHeights(); }

    public static int getCurrentWorld()
    {
        return DreamBotRebornApi.requireClient().getWorld();
    }

    public static String getUsername()
    {
        return DreamBotRebornApi.requireClient().getUsername();
    }

    public static int getMyPlayerIndex()
    {
        com.dreambotreborn.api.wrappers.interactive.Player player =
            com.dreambotreborn.api.methods.interactive.Players.getLocal();
        return player == null ? -1 : player.index;
    }

    public static Canvas getCanvas()
    {
        return DreamBotRebornApi.requireClient().getCanvas();
    }

    public static int getViewportWidth()
    {
        return DreamBotRebornApi.requireClient().getViewportWidth();
    }

    public static int getViewportHeight()
    {
        return DreamBotRebornApi.requireClient().getViewportHeight();
    }

    public static Tile getDestination()
    {
        WorldPoint destination = Walking.getDestination();
        return destination == null ? null : new Tile(destination);
    }

    public static int getDestX()
    {
        Tile destination = getDestination();
        return destination == null ? -1 : destination.getX();
    }

    public static int getDestY()
    {
        Tile destination = getDestination();
        return destination == null ? -1 : destination.getY();
    }

    public static List<com.dreambotreborn.api.wrappers.graphics.Projectile> getProjectiles()
    {
        return com.dreambotreborn.api.methods.interactive.Projectiles.all();
    }

    public static List<com.dreambotreborn.api.wrappers.graphics.GraphicsObject> getGraphicsObjects()
    {
        return com.dreambotreborn.api.methods.interactive.GraphicsObjects.all();
    }

    public static List<com.dreambotreborn.api.wrappers.interactive.Entity> getEntities()
    {
        List<com.dreambotreborn.api.wrappers.interactive.Entity> result = new ArrayList<>();
        result.addAll(com.dreambotreborn.api.methods.interactive.GameObjects.all().all());
        result.addAll(com.dreambotreborn.api.methods.interactive.NPCs.all().all());
        result.addAll(com.dreambotreborn.api.methods.interactive.Players.all().all());
        result.addAll(com.dreambotreborn.api.methods.item.GroundItems.all().all());
        return result;
    }

    public static com.dreambotreborn.api.wrappers.map.impl.CollisionMap[] getCollisionMaps()
    {
        net.runelite.api.CollisionData[] values = DreamBotRebornApi.requireClient().getCollisionMaps();
        if (values == null) return new com.dreambotreborn.api.wrappers.map.impl.CollisionMap[0];
        com.dreambotreborn.api.wrappers.map.impl.CollisionMap[] result =
            new com.dreambotreborn.api.wrappers.map.impl.CollisionMap[values.length];
        for (int i = 0; i < values.length; i++)
            result[i] = values[i] == null ? null
                : new com.dreambotreborn.api.wrappers.map.impl.CollisionMap(values[i]);
        return result;
    }

    public static boolean isCameraInMotion()
    {
        return cameraInMotion
            || DreamBotRebornApi.requireClient().getCameraYaw() != DreamBotRebornApi.requireClient().getCameraYawTarget()
            || DreamBotRebornApi.requireClient().getCameraPitch() != DreamBotRebornApi.requireClient().getCameraPitchTarget();
    }
    public static void setCameraInMotion(boolean value) { cameraInMotion = value; }
    public static double seededRandom() { return ThreadLocalRandom.current().nextDouble(); }
    public static boolean isMembers()
    {
        return DreamBotRebornApi.requireClient().getWorldType().contains(WorldType.MEMBERS);
    }
    public static boolean hasMembersAccess() { return isMembers(); }
    public static boolean isIronman()
    {
        AccountType type = DreamBotRebornApi.requireClient().getAccountType();
        return type != null && type.isIronman();
    }
    public static boolean isGroupIronman()
    {
        AccountType type = DreamBotRebornApi.requireClient().getAccountType();
        return type != null && type.isGroupIronman();
    }
    public static boolean isUltimateIronman()
    {
        return DreamBotRebornApi.requireClient().getAccountType() == AccountType.ULTIMATE_IRONMAN;
    }
    public static boolean isDynamicRegion() { return DreamBotRebornApi.requireClient().isInInstancedRegion(); }
    public static int getRunescapeFps() { return getFPS(); }
    public static void gainFocus() { com.dreambotreborn.api.input.Keyboard.gainFocus(); }
    public static void loseFocus() { com.dreambotreborn.api.input.Keyboard.loseFocus(); }
    public static String getAccountIdentifier() { return getUsername(); }
    public static boolean logout()
    {
        if (!isLoggedIn()) return true;
        if (!com.dreambotreborn.api.methods.tabs.Tabs.open(
            com.dreambotreborn.api.methods.tabs.Tab.LOGOUT)) return false;
        com.dreambotreborn.api.wrappers.widgets.Widget widget =
            com.dreambotreborn.api.methods.widget.Widgets.find(value -> value.visible
                && (value.text.toLowerCase().contains("logout")
                    || value.actions.stream().anyMatch(action ->
                        action.toLowerCase().contains("logout")))).first();
        return widget != null && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }

    public static BufferedImage getCanvasImage()
    {
        Canvas canvas = getCanvas();
        if (canvas == null || canvas.getWidth() <= 0 || canvas.getHeight() <= 0) return null;
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try { canvas.paint(graphics); } finally { graphics.dispose(); }
        return image;
    }

    public static boolean hasFocus()
    {
        return getCanvas() != null && getCanvas().hasFocus();
    }

    public static net.runelite.api.Client unwrap()
    {
        return DreamBotRebornApi.requireClient();
    }
}
