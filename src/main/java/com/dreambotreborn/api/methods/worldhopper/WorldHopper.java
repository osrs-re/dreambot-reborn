package com.dreambotreborn.api.methods.worldhopper;

import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.methods.input.Keyboard;
import com.dreambotreborn.api.methods.world.World;
import com.dreambotreborn.api.methods.world.Worlds;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

/** World-switching facade; mutations are serialized onto the game thread. */
public final class WorldHopper
{
    private WorldHopper() { }

    public static boolean isWorldHopperOpen()
    {
        Widget list = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.WORLD_SWITCHER_LIST);
        return list != null && !list.isHidden();
    }

    public static boolean closeWorldHopper()
    {
        if (!isWorldHopperOpen()) return true;
        Keyboard.pressEsc();
        return true;
    }

    public static boolean openWorldHopper()
    {
        if (isWorldHopperOpen()) return true;
        ClientThread.invokeLater(() -> DreamBotRebornApi.requireClient().openWorldHopper());
        return true;
    }

    public static boolean hopWorld(World world)
    {
        if (world == null) return false;
        ClientThread.invokeLater(() -> DreamBotRebornApi.requireClient().hopToWorld(world.unwrap()));
        return true;
    }

    public static boolean hopWorld(int world) { return hopWorld(world, false); }
    public static boolean hopWorld(int world, boolean ignored)
    {
        return hopWorld(Worlds.getWorld(world));
    }
    public static boolean quickHop(int world) { return hopWorld(world); }
    public static int getDefaultWorld() { return Worlds.getCurrentWorld(); }
    public static boolean changeWorldDirect(World world) { return hopWorld(world); }
}
