package com.dreambotreborn.api.methods.container.impl;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.ContainerQueries;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.methods.input.Keyboard;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.items.Item;
import net.runelite.api.widgets.WidgetID;

/** General-store and specialist-shop facade backed by the live shop widgets. */
public final class Shop
{
    private static final ContainerType TYPE = ContainerType.SHOP;
    private Shop() { }

    public static int capacity() { return Containers.capacity(TYPE); }
    public static Query<Item> all() { return ContainerQueries.all(TYPE); }
    public static Query<Item> all(Predicate<? super Item> predicate)
    {
        return ContainerQueries.all(TYPE, predicate);
    }
    public static boolean isOpen()
    {
        return Widgets.all().stream().anyMatch(widget ->
            widget.groupId == WidgetID.SHOP_GROUP_ID && widget.visible);
    }
    public static boolean open()
    {
        NPC npc = NPCs.closest(value -> value.hasAction("Trade"));
        return npc != null && npc.interact("Trade");
    }
    public static boolean open(String name)
    {
        NPC npc = NPCs.closest(value -> value.name.equalsIgnoreCase(name)
            && value.hasAction("Trade"));
        return npc != null && npc.interact("Trade");
    }
    public static boolean open(int id)
    {
        NPC npc = NPCs.closest(value -> value.id == id && value.hasAction("Trade"));
        return npc != null && npc.interact("Trade");
    }
    public static boolean close()
    {
        if (!isOpen()) return true;
        Keyboard.pressEsc();
        return true;
    }
    public static String getTitle()
    {
        com.dreambotreborn.api.wrappers.widgets.Widget title = Widgets.find(widget ->
            widget.groupId == WidgetID.SHOP_GROUP_ID && widget.visible
                && !widget.text.isEmpty()).first();
        return title == null ? null : title.text;
    }
    public static com.dreambotreborn.api.methods.widget.Widget getParent()
    {
        return Widgets.getWidget(WidgetID.SHOP_GROUP_ID);
    }
    public static Rectangle getSlotBounds(Item item)
    {
        return item == null ? null : getSlotBounds(item.slot);
    }
    public static Rectangle getSlotBounds(int slot)
    {
        net.runelite.api.widgets.Widget widget = Containers.widgetForSlot(TYPE, slot);
        return widget == null || widget.getBounds() == null ? null : new Rectangle(widget.getBounds());
    }

    public static boolean sell(int id, int amount) { return sell(Inventory.get(id), amount); }
    public static boolean sell(String name, int amount) { return sell(Inventory.get(name), amount); }
    public static boolean sell(Predicate<? super Item> filter, int amount)
    {
        return sell(Inventory.get(filter), amount);
    }
    public static boolean sell(Item item, int amount) { return quantity(item, "Sell", amount); }
    public static boolean purchase(int id, int amount) { return purchase(get(id), amount); }
    public static boolean purchase(String name, int amount) { return purchase(get(name), amount); }
    public static boolean purchase(Predicate<? super Item> filter, int amount)
    {
        return purchase(get(filter), amount);
    }
    public static boolean purchase(Item item, int amount) { return quantity(item, "Buy", amount); }

    public static boolean sellOne(int id) { return sell(id, 1); }
    public static boolean sellOne(String name) { return sell(name, 1); }
    public static boolean sellOne(Item item) { return sell(item, 1); }
    public static boolean sellOne(Predicate<? super Item> filter) { return sell(filter, 1); }
    public static boolean sellFive(int id) { return sell(id, 5); }
    public static boolean sellFive(String name) { return sell(name, 5); }
    public static boolean sellFive(Item item) { return sell(item, 5); }
    public static boolean sellFive(Predicate<? super Item> filter) { return sell(filter, 5); }
    public static boolean sellTen(int id) { return sell(id, 10); }
    public static boolean sellTen(String name) { return sell(name, 10); }
    public static boolean sellTen(Item item) { return sell(item, 10); }
    public static boolean sellTen(Predicate<? super Item> filter) { return sell(filter, 10); }
    public static boolean sellFifty(int id) { return sell(id, 50); }
    public static boolean sellFifty(String name) { return sell(name, 50); }
    public static boolean sellFifty(Item item) { return sell(item, 50); }
    public static boolean sellFifty(Predicate<? super Item> filter) { return sell(filter, 50); }
    public static boolean purchaseOne(int id) { return purchase(id, 1); }
    public static boolean purchaseOne(String name) { return purchase(name, 1); }
    public static boolean purchaseOne(Item item) { return purchase(item, 1); }
    public static boolean purchaseOne(Predicate<? super Item> filter) { return purchase(filter, 1); }
    public static boolean purchaseFive(int id) { return purchase(id, 5); }
    public static boolean purchaseFive(String name) { return purchase(name, 5); }
    public static boolean purchaseFive(Item item) { return purchase(item, 5); }
    public static boolean purchaseFive(Predicate<? super Item> filter) { return purchase(filter, 5); }
    public static boolean purchaseTen(int id) { return purchase(id, 10); }
    public static boolean purchaseTen(String name) { return purchase(name, 10); }
    public static boolean purchaseTen(Item item) { return purchase(item, 10); }
    public static boolean purchaseTen(Predicate<? super Item> filter) { return purchase(filter, 10); }
    public static boolean purchaseFifty(int id) { return purchase(id, 50); }
    public static boolean purchaseFifty(String name) { return purchase(name, 50); }
    public static boolean purchaseFifty(Item item) { return purchase(item, 50); }
    public static boolean purchaseFifty(Predicate<? super Item> filter) { return purchase(filter, 50); }
    public static boolean interact(Predicate<? super Item> filter, String action)
    {
        Item item = get(filter);
        return item != null && item.interact(action);
    }

    public static Item getItemInSlot(int slot) { return ContainerQueries.getInSlot(TYPE, slot); }
    public static Item get(int id) { return ContainerQueries.get(TYPE, id); }
    public static Item get(String name) { return ContainerQueries.get(TYPE, name); }
    public static Item get(int... ids) { return get(item -> Queries.id(item.id, ids)); }
    public static Item get(String... names) { return get(item -> Queries.name(item.name, names)); }
    public static Item get(Predicate<? super Item> filter) { return ContainerQueries.get(TYPE, filter); }
    public static Query<Item> except(Predicate<? super Item> filter)
    {
        return all(item -> !filter.test(item));
    }
    public static int size() { return fullSlotCount(); }
    public static boolean isEmpty() { return size() == 0; }
    public static boolean isFull() { return emptySlotCount() == 0; }
    public static int fullSlotCount() { return ContainerQueries.fullSlots(TYPE); }
    public static int emptySlotCount() { return ContainerQueries.emptySlots(TYPE); }
    public static boolean isSlotEmpty(int slot) { return getItemInSlot(slot) == null; }
    public static boolean isSlotFull(int slot) { return !isSlotEmpty(slot); }
    public static boolean contains(int id) { return get(id) != null; }
    public static boolean contains(String name) { return get(name) != null; }
    public static boolean contains(int... ids) { return get(ids) != null; }
    public static boolean contains(String... names) { return get(names) != null; }
    public static boolean contains(Predicate<? super Item> filter) { return get(filter) != null; }
    public static boolean contains(Object value)
    {
        if (value instanceof Number) return contains(((Number) value).intValue());
        if (value instanceof String) return contains((String) value);
        return value instanceof Item && contains(((Item) value).id);
    }
    public static boolean containsAll(String... names) { return ContainerQueries.containsAllNames(TYPE, names); }
    public static boolean containsAll(int... ids) { return ContainerQueries.containsAllIds(TYPE, ids); }
    public static boolean containsAll(Collection<?> values) { return ContainerQueries.containsAll(TYPE, values); }
    public static int count(int id) { return ContainerQueries.count(TYPE, item -> item.id == id); }
    public static int count(String name)
    {
        return ContainerQueries.count(TYPE, item -> item.name.equalsIgnoreCase(name));
    }
    public static int count(Predicate<? super Item> filter) { return ContainerQueries.count(TYPE, filter); }
    public static boolean onlyContains(String... names)
    {
        return ContainerQueries.onlyContains(TYPE, item -> Queries.name(item.name, names));
    }
    public static boolean onlyContains(int... ids)
    {
        return ContainerQueries.onlyContains(TYPE, item -> Queries.id(item.id, ids));
    }
    public static boolean onlyContains(Predicate<? super Item> filter)
    {
        return ContainerQueries.onlyContains(TYPE, filter);
    }
    public static boolean slotContains(int slot, String... names)
    {
        Item item = getItemInSlot(slot); return item != null && Queries.name(item.name, names);
    }
    public static boolean slotContains(int slot, int... ids)
    {
        Item item = getItemInSlot(slot); return item != null && Queries.id(item.id, ids);
    }
    public static boolean slotContains(int slot, Item item)
    {
        return item != null && slotContains(slot, item.id);
    }
    public static boolean slotContains(int slot, Predicate<? super Item> filter)
    {
        Item item = getItemInSlot(slot); return item != null && filter.test(item);
    }
    public static boolean slotNameContains(int slot, String text)
    {
        Item item = getItemInSlot(slot);
        return item != null && text != null && item.name.toLowerCase().contains(text.toLowerCase());
    }
    public static int getFirstEmptySlot()
    {
        for (int slot = 0; slot < capacity(); slot++) if (isSlotEmpty(slot)) return slot;
        return -1;
    }
    public static int getFirstFullSlot() { Item item = all().first(); return item == null ? -1 : item.slot; }
    public static int getIdForSlot(int slot) { Item item = getItemInSlot(slot); return item == null ? -1 : item.id; }
    public static String getNameForSlot(int slot) { Item item = getItemInSlot(slot); return item == null ? null : item.name; }
    public static int slot(int id) { return ContainerQueries.slot(TYPE, item -> item.id == id); }
    public static int slot(String name)
    {
        return ContainerQueries.slot(TYPE, item -> item.name.equalsIgnoreCase(name));
    }
    public static int slot(Predicate<? super Item> filter) { return ContainerQueries.slot(TYPE, filter); }
    public static Object[] toArray() { return all().all().toArray(); }

    private static boolean quantity(Item item, String verb, int amount)
    {
        if (!isOpen() || item == null || amount <= 0) return false;
        String exact = verb + " " + amount;
        if (item.hasAction(exact)) return item.interact(exact);
        String hyphen = verb + "-" + amount;
        if (item.hasAction(hyphen)) return item.interact(hyphen);
        return amount == 1 && item.interact(verb + " 1");
    }
}
