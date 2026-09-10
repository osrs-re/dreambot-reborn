package com.dreambotreborn.api.methods.ignore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public class IgnoredProvider
{
    public static List<Ignored> all()
    {
        net.runelite.api.NameableContainer<net.runelite.api.Ignore> container =
            DreamBotRebornApi.requireClient().getIgnoreContainer();
        net.runelite.api.Ignore[] values = container == null ? null : container.getMembers();
        if (values == null) return Collections.emptyList();
        List<Ignored> result = new ArrayList<>();
        for (net.runelite.api.Ignore value : values) if (value != null) result.add(new Ignored(value));
        return Collections.unmodifiableList(result);
    }
    public static int getSize()
    {
        net.runelite.api.NameableContainer<net.runelite.api.Ignore> container =
            DreamBotRebornApi.requireClient().getIgnoreContainer();
        return container == null ? 0 : container.getCount();
    }
    public static boolean open() { return Tabs.open(Tab.FRIENDS); }
    public static boolean isIgnoredOpen() { return Tabs.isOpen(Tab.FRIENDS); }
    public static boolean isIgnored(String name) { return get(name) != null; }
    public static Ignored get(String name)
    {
        if (name == null) return null;
        for (Ignored ignored : all()) if (ignored.getName().equalsIgnoreCase(name)) return ignored;
        return null;
    }
    public static boolean add(String name)
    {
        if (name == null || name.trim().isEmpty() || !open()) return false;
        WidgetChild add = Widgets.getMatchingWidget(widget -> widget.visible
            && (widget.hasAction("Add ignore") || widget.text.equalsIgnoreCase("Add ignore")));
        return add != null && (add.hasAction("Add ignore") ? add.interact("Add ignore") : add.click())
            && Await.success(com.dreambotreborn.api.input.Keyboard.type(name, true));
    }
    public static boolean add(Player player) { return player != null && add(player.getName()); }
    public static boolean remove(String name)
    {
        if (name == null || !open()) return false;
        WidgetChild ignored = Widgets.getMatchingWidget(widget -> widget.visible
            && (widget.text.equalsIgnoreCase(name) || widget.name.equalsIgnoreCase(name))
            && widget.hasAction("Delete"));
        return ignored != null && ignored.interact("Delete");
    }
    public static boolean remove(Player player) { return player != null && remove(player.getName()); }
}
