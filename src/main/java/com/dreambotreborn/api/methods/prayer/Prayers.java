package com.dreambotreborn.api.methods.prayer;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Varbits;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;
import net.runelite.api.widgets.WidgetInfo;

/** Prayer-state queries. */
public final class Prayers
{
    private Prayers()
    {
    }

    public static boolean isActive(Prayer prayer)
    {
        return prayer != null && DreamBotRebornApi.requireClient().isPrayerActive(prayer.unwrap());
    }

    public static boolean isActive(net.runelite.api.Prayer prayer)
    {
        return prayer != null && DreamBotRebornApi.requireClient().isPrayerActive(prayer);
    }

    public static Prayer[] getActive()
    {
        List<Prayer> result = new ArrayList<>();
        for (Prayer prayer : Prayer.values())
        {
            if (isActive(prayer))
            {
                result.add(prayer);
            }
        }
        return result.toArray(new Prayer[0]);
    }

    public static boolean isQuickPrayerActive()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    public static boolean openTab() { return Tabs.open(Tab.PRAYER); }
    public static boolean isOpen() { return Tabs.isOpen(Tab.PRAYER); }
    public static WidgetChild getWidgetChild(Prayer prayer)
    {
        return prayer == null ? null : prayer.getWidgetChild();
    }
    public static WidgetChild getWidgetChildQuickPrayer(Prayer prayer)
    {
        net.runelite.api.widgets.Widget root = DreamBotRebornApi.requireClient().getWidget(
            WidgetInfo.QUICK_PRAYER_PRAYERS);
        if (root == null || prayer == null) return null;
        net.runelite.api.widgets.Widget child = root.getChild(prayer.getQuickPrayerChild());
        return child == null ? null : new WidgetChild(child);
    }
    public static boolean toggle(boolean active, Prayer prayer)
    {
        if (prayer == null || !prayer.isUnlocked()) return false;
        if (isActive(prayer) == active) return true;
        if (!openTab()) return false;
        WidgetChild widget = prayer.getWidgetChild();
        return widget != null && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }
    public static boolean flick(Prayer prayer, int milliseconds)
    {
        if (!toggle(true, prayer)) return false;
        com.dreambotreborn.api.utilities.Sleep.sleep(Math.max(1, milliseconds));
        return toggle(false, prayer);
    }
    public static boolean toggleQuickPrayer(boolean active)
    {
        if (isQuickPrayerActive() == active) return true;
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(
            WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        return widget != null && new WidgetChild(widget).click();
    }
    public static List<Prayer> getQuickPrayers()
    {
        List<Prayer> result = new ArrayList<>();
        net.runelite.api.widgets.Widget root = DreamBotRebornApi.requireClient().getWidget(
            WidgetInfo.QUICK_PRAYER_PRAYERS);
        if (root == null) return result;
        for (Prayer prayer : Prayer.values())
        {
            WidgetChild child = getWidgetChildQuickPrayer(prayer);
            if (child != null && child.unwrap().getSpriteId() != -1 && child.visible)
                result.add(prayer);
        }
        return result;
    }
    public static boolean setupQuickPrayers(Prayer... prayers)
    {
        net.runelite.api.widgets.Widget orb = DreamBotRebornApi.requireClient().getWidget(
            WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        if (orb == null || !Mouse.click(orb.getBounds(), true)
            || !com.dreambotreborn.api.wrappers.widgets.Menu.clickAction("Setup")) return false;
        if (prayers != null) for (Prayer prayer : prayers)
        {
            WidgetChild widget = getWidgetChildQuickPrayer(prayer);
            if (widget != null) widget.click();
        }
        com.dreambotreborn.api.wrappers.widgets.Widget done = Widgets.find(widget ->
            widget.visible && (widget.text.equalsIgnoreCase("Done")
                || widget.actions.stream().anyMatch(action -> action.equalsIgnoreCase("Done")))).first();
        return done != null && (done.actions.isEmpty() ? done.click() : done.interact());
    }
    public static boolean isPrayerFilteringEnabled()
    {
        return filteringEnabled;
    }
    public static boolean setPrayerFilteringEnabled(boolean enabled)
    {
        if (filteringEnabled == enabled) return true;
        com.dreambotreborn.api.wrappers.widgets.Widget filter = Widgets.find(widget ->
            widget.groupId == 541 && widget.visible && (widget.text.toLowerCase().contains("filter")
                || widget.actions.stream().anyMatch(action ->
                    action.toLowerCase().contains("filter")))).first();
        if (filter == null || !(filter.actions.isEmpty() ? filter.click() : filter.interact()))
            return false;
        filteringEnabled = enabled;
        return true;
    }

    private static volatile boolean filteringEnabled;
}
