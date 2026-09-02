package com.dreambotreborn.api.methods.trade;

import java.awt.Rectangle;
import java.util.Arrays;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.internal.ContainerQueries;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.input.Keyboard;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.wrappers.items.Item;

/** Player-to-player trade facade for both offer containers and confirmation stages. */
public final class Trade
{
    private static final int CONFIRM_GROUP = 334;
    private static final int TRADE_GROUP = 335;
    private Trade() { }

    public static Item[] getItems(boolean theirs)
    {
        return ContainerQueries.all(theirs ? ContainerType.TRADE_OTHER : ContainerType.TRADE)
            .all().toArray(new Item[0]);
    }
    public static Item[] getMyItems() { return getItems(false); }
    public static Item[] getTheirItems() { return getItems(true); }
    public static boolean isOpen() { return isOpen(1) || isOpen(2); }
    public static boolean isOpen(int stage)
    {
        int group = stage == 2 ? CONFIRM_GROUP : TRADE_GROUP;
        return Widgets.all().stream().anyMatch(widget -> widget.groupId == group && widget.visible);
    }
    public static String getFirstTitle()
    {
        com.dreambotreborn.api.wrappers.widgets.Widget widget = Widgets.find(value ->
            value.groupId == TRADE_GROUP && value.visible && !value.text.isEmpty()).first();
        return widget == null ? null : widget.text;
    }
    public static String getTradingWith()
    {
        String title = getFirstTitle();
        if (title == null) return null;
        int colon = title.indexOf(':');
        return colon < 0 ? title : title.substring(colon + 1).trim();
    }
    public static boolean canAccept()
    {
        return control("Accept") != null;
    }
    public static boolean acceptTrade() { return acceptTrade(isOpen(2) ? 2 : 1); }
    public static boolean acceptTrade(int stage)
    {
        if (!isOpen(stage)) return false;
        com.dreambotreborn.api.wrappers.widgets.Widget widget = control("Accept");
        return widget != null && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }
    public static boolean hasAcceptedTrade(TradeUser user)
    {
        String needle = user == TradeUser.US ? "waiting for other player" : "other player has accepted";
        return Widgets.find(widget -> (widget.groupId == TRADE_GROUP || widget.groupId == CONFIRM_GROUP)
            && widget.visible && widget.text.toLowerCase().contains(needle)).exists();
    }
    public static boolean declineTrade() { return declineTrade(isOpen(2) ? 2 : 1); }
    public static boolean declineTrade(int stage)
    {
        if (!isOpen(stage)) return false;
        com.dreambotreborn.api.wrappers.widgets.Widget widget = control("Decline");
        return widget != null && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }
    public static boolean close() { return close(isOpen(2) ? 2 : 1); }
    public static boolean close(int stage)
    {
        if (!isOpen(stage)) return true;
        Keyboard.pressEsc();
        return true;
    }
    public static boolean verifyTrade(boolean theirs, Item[] expected)
    {
        Item[] actual = getItems(theirs);
        if (expected == null) return actual.length == 0;
        for (Item wanted : expected)
        {
            if (wanted == null) continue;
            int quantity = 0;
            for (Item item : actual) if (item.id == wanted.id) quantity += item.amount;
            if (quantity < wanted.amount) return false;
        }
        return true;
    }
    public static boolean contains(boolean theirs, int amount, String name)
    {
        Item item = getItem(theirs, name);
        return item != null && item.amount >= amount;
    }
    public static boolean contains(boolean theirs, String... names)
    {
        if (names == null) return true;
        for (String name : names) if (getItem(theirs, name) == null) return false;
        return true;
    }
    public static boolean contains(boolean theirs, int amount, int id)
    {
        Item item = getItem(theirs, id);
        return item != null && item.amount >= amount;
    }
    public static boolean contains(boolean theirs, int... ids)
    {
        if (ids == null) return true;
        for (int id : ids) if (getItem(theirs, id) == null) return false;
        return true;
    }
    public static int getValue(boolean theirs)
    {
        long value = 0;
        for (Item item : getItems(theirs)) value += (long) item.getValue() * item.amount;
        return (int) Math.min(Integer.MAX_VALUE, value);
    }
    public static Item getItem(boolean theirs, String name)
    {
        for (Item item : getItems(theirs))
            if (name != null && item.name.equalsIgnoreCase(name)) return item;
        return null;
    }
    public static Item getItem(boolean theirs, int id)
    {
        for (Item item : getItems(theirs)) if (item.id == id) return item;
        return null;
    }
    public static boolean tradeWithPlayer(String name)
    {
        return tradeWithPlayer(Players.closest(player ->
            name != null && player.name.equalsIgnoreCase(name)));
    }
    public static boolean tradeWithPlayer(Player player)
    {
        return player != null && player.interact("Trade with");
    }
    public static boolean addItem(String name, int amount) { return addItem(Inventory.get(name), amount); }
    public static boolean addItem(int id, int amount) { return addItem(Inventory.get(id), amount); }
    public static boolean addItem(Item item, int amount) { return quantity(item, "Offer", amount); }
    public static boolean removeItem(String name, int amount)
    {
        return removeItem(getItem(false, name), amount);
    }
    public static boolean removeItem(int id, int amount) { return removeItem(getItem(false, id), amount); }
    public static boolean removeItem(Item item, int amount) { return quantity(item, "Remove", amount); }
    public static Rectangle slotBounds(int slot)
    {
        Item item = ContainerQueries.getInSlot(ContainerType.TRADE, slot);
        return item == null ? null : item.getBounds();
    }

    private static boolean quantity(Item item, String verb, int amount)
    {
        if (!isOpen(1) || item == null || amount <= 0) return false;
        String amountText = amount == Integer.MAX_VALUE ? "All" : Integer.toString(amount);
        String[] actions = {verb + " " + amountText, verb + "-" + amountText};
        for (String action : actions) if (item.hasAction(action)) return item.interact(action);
        String x = verb + " X";
        if (!item.hasAction(x)) x = verb + "-X";
        if (item.hasAction(x) && item.interact(x))
        {
            com.dreambotreborn.api.methods.input.Keyboard.type(amount, true);
            return true;
        }
        return false;
    }

    private static com.dreambotreborn.api.wrappers.widgets.Widget control(String action)
    {
        return Widgets.find(widget -> (widget.groupId == TRADE_GROUP
            || widget.groupId == CONFIRM_GROUP) && widget.visible
            && (widget.text.equalsIgnoreCase(action)
                || widget.actions.stream().anyMatch(value -> value.equalsIgnoreCase(action)))).first();
    }
}
