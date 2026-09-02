package com.dreambotreborn.api.methods.emotes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public final class Emotes
{
    private static final int GROUP = 216;
    private static final int CONTENTS = 2;
    private Emotes() { }
    public static WidgetChild getEmoteContainer()
    {
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(GROUP, CONTENTS);
        return widget == null ? null : new WidgetChild(widget);
    }
    public static WidgetChild getEmoteChild(Emote emote)
    {
        if (emote == null) return null;
        WidgetChild root = getEmoteContainer();
        if (root == null) return null;
        String expected = emote.name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
        List<net.runelite.api.widgets.Widget> stack = new ArrayList<>();
        stack.add(root.unwrap());
        for (int cursor = 0; cursor < stack.size(); cursor++)
        {
            net.runelite.api.widgets.Widget widget = stack.get(cursor);
            String text = ((widget.getName() == null ? "" : widget.getName()) + ' '
                + (widget.getText() == null ? "" : widget.getText())).replaceAll("<[^>]*>", "")
                .toLowerCase(Locale.ENGLISH);
            if (text.contains(expected)) return new WidgetChild(widget);
            add(stack, widget.getChildren()); add(stack, widget.getDynamicChildren());
            add(stack, widget.getStaticChildren()); add(stack, widget.getNestedChildren());
        }
        net.runelite.api.widgets.Widget fallback = root.unwrap().getChild(emote.childwidget);
        return fallback == null ? null : new WidgetChild(fallback);
    }
    public static boolean openTab() { return Tabs.open(Tab.EMOTES); }
    public static boolean isTabOpen() { return Tabs.isOpen(Tab.EMOTES); }
    public static boolean doEmote(Emote emote)
    {
        if (!openTab()) return false;
        WidgetChild widget = getEmoteChild(emote);
        return widget != null && widget.visible
            && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }
    public static boolean doRandomEmote()
    {
        List<Emote> available = new ArrayList<>();
        for (Emote emote : Emote.values())
        {
            WidgetChild widget = getEmoteChild(emote);
            if (widget != null && widget.visible) available.add(emote);
        }
        return !available.isEmpty() && doEmote(
            available.get(ThreadLocalRandom.current().nextInt(available.size())));
    }
    private static void add(List<net.runelite.api.widgets.Widget> target,
                            net.runelite.api.widgets.Widget[] values)
    {
        if (values != null) for (net.runelite.api.widgets.Widget value : values)
            if (value != null && !target.contains(value)) target.add(value);
    }
}
