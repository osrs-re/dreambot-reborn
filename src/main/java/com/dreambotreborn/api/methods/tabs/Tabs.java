package com.dreambotreborn.api.methods.tabs;

import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.wrappers.widgets.Widget;
import com.dreambotreborn.api.utilities.Await;
import net.runelite.api.VarClientInt;
import net.runelite.api.widgets.WidgetInfo;

/** Side-panel tab state and mouse-driven opening. */
public final class Tabs
{
    private Tabs()
    {
    }

    public static boolean isOpen(Tab tab)
    {
        return tab != null
            && DreamBotRebornApi.requireClient().getVarcIntValue(VarClientInt.INVENTORY_TAB) == tab.getIndex();
    }

    public static Tab getOpen()
    {
        int index = DreamBotRebornApi.requireClient().getVarcIntValue(VarClientInt.INVENTORY_TAB);
        for (Tab tab : Tab.values())
        {
            if (tab.getIndex() == index)
            {
                return tab;
            }
        }
        return null;
    }

    public static boolean open(Tab tab)
    {
        return Await.success(openAsync(tab));
    }

    public static CompletableFuture<Boolean> openAsync(Tab tab)
    {
        if (tab == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        if (isOpen(tab))
        {
            return CompletableFuture.completedFuture(true);
        }
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(info(tab));
        return widget == null ? CompletableFuture.completedFuture(false) : new Widget(widget).clickAsync();
    }

    public static boolean openWithMouse(Tab tab)
    {
        return open(tab);
    }

    public static CompletableFuture<Boolean> openWithMouseAsync(Tab tab)
    {
        return openAsync(tab);
    }

    public static boolean isDisabled(Tab tab)
    {
        if (tab == null)
        {
            return true;
        }
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(info(tab));
        return widget == null || widget.isHidden();
    }

    private static WidgetInfo info(Tab tab)
    {
        boolean resized = DreamBotRebornApi.requireClient().isResized();
        WidgetInfo[] fixed =
        {
            WidgetInfo.FIXED_VIEWPORT_COMBAT_TAB,
            WidgetInfo.FIXED_VIEWPORT_STATS_TAB,
            WidgetInfo.FIXED_VIEWPORT_QUESTS_TAB,
            WidgetInfo.FIXED_VIEWPORT_INVENTORY_TAB,
            WidgetInfo.FIXED_VIEWPORT_EQUIPMENT_TAB,
            WidgetInfo.FIXED_VIEWPORT_PRAYER_TAB,
            WidgetInfo.FIXED_VIEWPORT_MAGIC_TAB,
            WidgetInfo.FIXED_VIEWPORT_FRIENDS_CHAT_TAB,
            WidgetInfo.FIXED_VIEWPORT_FRIENDS_TAB,
            WidgetInfo.FIXED_VIEWPORT_IGNORES_TAB,
            WidgetInfo.FIXED_VIEWPORT_LOGOUT_TAB,
            WidgetInfo.FIXED_VIEWPORT_OPTIONS_TAB,
            WidgetInfo.FIXED_VIEWPORT_EMOTES_TAB,
            WidgetInfo.FIXED_VIEWPORT_MUSIC_TAB
        };
        WidgetInfo[] resize =
        {
            WidgetInfo.RESIZABLE_VIEWPORT_COMBAT_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_STATS_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_QUESTS_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_INVENTORY_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_EQUIPMENT_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_PRAYER_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_CHAT_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_FRIENDS_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_IGNORES_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_LOGOUT_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_OPTIONS_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_EMOTES_TAB,
            WidgetInfo.RESIZABLE_VIEWPORT_MUSIC_TAB
        };
        return (resized ? resize : fixed)[tab.getIndex()];
    }
}
