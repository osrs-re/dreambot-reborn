package com.dreambotreborn.api.methods.input;

import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import net.runelite.api.coords.WorldPoint;

/** Camera state and direct target rotation. */
public final class Camera
{
    private Camera()
    {
    }

    public static int getPitch()
    {
        return DreamBotRebornApi.requireClient().getCameraPitch();
    }

    public static int getYaw()
    {
        return DreamBotRebornApi.requireClient().getCameraYaw();
    }

    public static int getX()
    {
        return DreamBotRebornApi.requireClient().getCameraX();
    }

    public static int getY()
    {
        return DreamBotRebornApi.requireClient().getCameraY();
    }

    public static int getZ()
    {
        return DreamBotRebornApi.requireClient().getCameraZ();
    }

    public static boolean rotateTo(int yaw, int pitch)
    {
        return rotateToYaw(yaw) && rotateToPitch(pitch);
    }

    public static boolean rotateToYaw(int yaw)
    {
        int target = Math.floorMod(yaw, 2048);
        if (ClientThread.isClientThread()) DreamBotRebornApi.requireClient().setCameraYawTarget(target);
        else ClientThread.invokeLater(() -> DreamBotRebornApi.requireClient().setCameraYawTarget(target));
        return true;
    }

    public static boolean rotateToPitch(int pitch)
    {
        int target = Math.max(128, Math.min(383, pitch));
        if (ClientThread.isClientThread()) DreamBotRebornApi.requireClient().setCameraPitchTarget(target);
        else ClientThread.invokeLater(() -> DreamBotRebornApi.requireClient().setCameraPitchTarget(target));
        return true;
    }

    public static boolean rotateToEntity(Entity entity)
    {
        return entity != null && rotateToTile(entity.getTile());
    }

    public static boolean rotateToTile(Tile tile)
    {
        if (tile == null) return false;
        net.runelite.api.Player player = DreamBotRebornApi.requireClient().getLocalPlayer();
        WorldPoint origin = player == null ? null : player.getWorldLocation();
        if (origin == null) return false;
        int deltaX = tile.getX() - origin.getX();
        int deltaY = tile.getY() - origin.getY();
        int yaw = (int) Math.round(Math.atan2(deltaX, deltaY) * 1024.0 / Math.PI) & 2047;
        return rotateTo(yaw, Math.max(220, getPitch()));
    }

    public static int getYawForTile(Tile tile)
    {
        net.runelite.api.Player player = DreamBotRebornApi.requireClient().getLocalPlayer();
        if (tile == null || player == null) return getYaw();
        WorldPoint origin = player.getWorldLocation();
        return (int) Math.round(Math.atan2(tile.getX() - origin.getX(),
            tile.getY() - origin.getY()) * 1024.0 / Math.PI) & 2047;
    }

    public static int getYawForEntity(Entity entity)
    {
        return entity == null ? getYaw() : getYawForTile(entity.getTile());
    }
}
