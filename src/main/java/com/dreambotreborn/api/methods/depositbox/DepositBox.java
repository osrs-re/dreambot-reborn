package com.dreambotreborn.api.methods.depositbox;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.function.Predicate;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.methods.input.Keyboard;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;

/** Deposit-box operations performed through the visible inventory widgets. */
public final class DepositBox
{
    private DepositBox() { }
    public static int widgetParentId() { return WidgetID.DEPOSIT_BOX_GROUP_ID; }
    public static int widgetChildId()
    {
        return WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.getChildId();
    }
    public static int capacity() { return Inventory.capacity(); }
    public static Query<Item> all() { return Inventory.all(); }
    public static Query<Item> all(Predicate<? super Item> filter) { return Inventory.all(filter); }
    public static boolean isOpen()
    {
        return Widgets.all().stream().anyMatch(widget ->
            widget.groupId == widgetParentId() && widget.visible);
    }
    public static boolean open() { return openClosest(); }
    public static boolean openClosest()
    {
        Entity object = GameObjects.closest(value -> value.hasAction("Deposit"));
        Entity npc = NPCs.closest(value -> value.hasAction("Deposit"));
        Entity closest = object == null ? npc : npc == null ? object
            : object.distance() <= npc.distance() ? object : npc;
        return closest != null && closest.interact("Deposit");
    }
    public static boolean close()
    {
        if (!isOpen()) return true;
        Keyboard.pressEsc();
        return true;
    }
    public static boolean depositAllItems() { return clickControl("Deposit inventory"); }
    public static boolean depositAllEquipment() { return clickControl("Deposit worn"); }
    public static boolean depositAllLoot() { return clickControl("Deposit loot"); }
    public static boolean deposit(int id, int amount) { return deposit(Inventory.get(id), amount); }
    public static boolean deposit(String name, int amount) { return deposit(Inventory.get(name), amount); }
    public static boolean deposit(Predicate<? super Item> filter, int amount)
    {
        return deposit(Inventory.get(filter), amount);
    }
    public static boolean deposit(Item item, int amount)
    {
        if (!isOpen() || item == null || amount <= 0) return false;
        String amountText = amount == Integer.MAX_VALUE ? "All" : Integer.toString(amount);
        String action = "Deposit-" + amountText;
        if (item.hasAction(action)) return item.interact(action);
        if (amount == 1 && item.hasAction("Deposit-1")) return item.interact("Deposit-1");
        if (item.hasAction("Deposit-X") && item.interact("Deposit-X"))
        {
            com.dreambotreborn.api.methods.input.Keyboard.type(amount, true);
            return true;
        }
        return false;
    }
    public static boolean deposit(int id) { return deposit(id, 1); }
    public static boolean deposit(String name) { return deposit(name, 1); }
    public static boolean deposit(Item item) { return deposit(item, 1); }
    public static boolean deposit(Predicate<? super Item> filter) { return deposit(filter, 1); }
    public static boolean depositAll(int id) { return deposit(id, Integer.MAX_VALUE); }
    public static boolean depositAll(String name) { return deposit(name, Integer.MAX_VALUE); }
    public static boolean depositAll(Item item) { return deposit(item, Integer.MAX_VALUE); }
    public static boolean depositAll(Predicate<? super Item> filter)
    {
        return deposit(filter, Integer.MAX_VALUE);
    }
    public static boolean depositAllExcept(String... names)
    {
        return depositAllExcept(item -> Queries.name(item.name, names));
    }
    public static boolean depositAllExcept(int... ids)
    {
        return depositAllExcept(item -> Queries.id(item.id, ids));
    }
    public static boolean depositAllExcept(Predicate<? super Item> keep)
    {
        if (keep == null) return false;
        boolean changed = false;
        for (Item item : Inventory.all(value -> !keep.test(value))) changed |= depositAll(item);
        return changed;
    }
    public static Rectangle slotBounds(int slot) { return Inventory.slotBounds(slot); }
    public static WidgetChild getSlotWidget(int slot) { return Inventory.getWidgetForSlot(slot); }
    public static int fullSlotCount() { return Inventory.fullSlotCount(); }
    public static int emptySlotCount() { return Inventory.emptySlotCount(); }
    public static boolean isSlotEmpty(int slot) { return Inventory.isSlotEmpty(slot); }
    public static boolean isSlotFull(int slot) { return Inventory.isSlotFull(slot); }
    public static boolean onlyContains(String... names) { return Inventory.onlyContains(names); }
    public static boolean onlyContains(int... ids) { return Inventory.onlyContains(ids); }
    public static boolean onlyContains(Predicate<? super Item> filter) { return Inventory.onlyContains(filter); }
    public static boolean slotContains(int slot, String... names) { return Inventory.slotContains(slot, names); }
    public static boolean slotNameContains(int slot, String text) { return Inventory.slotNameContains(slot, text); }
    public static boolean slotContains(int slot, int... ids) { return Inventory.slotContains(slot, ids); }
    public static boolean slotContains(int slot, Item item)
    {
        return item != null && Inventory.slotContains(slot, item.id);
    }
    public static boolean slotContains(int slot, Predicate<? super Item> filter)
    {
        return Inventory.slotContains(slot, filter);
    }
    public static int getFirstEmptySlot() { return Inventory.getFirstEmptySlot(); }
    public static int getFirstFullSlot() { return Inventory.getFirstFullSlot(); }
    public static int getIdForSlot(int slot) { return Inventory.getIdForSlot(slot); }
    public static String getNameForSlot(int slot) { return Inventory.getNameForSlot(slot); }
    public static int slot(int id) { return Inventory.slot(id); }
    public static int slot(String name) { return Inventory.slot(name); }
    public static int slot(Predicate<? super Item> filter) { return Inventory.slot(filter); }
    public static Item getItemInSlot(int slot) { return Inventory.getItemInSlot(slot); }
    public static Item get(int id) { return Inventory.get(id); }
    public static Item get(String name) { return Inventory.get(name); }
    public static Item get(int... ids) { return Inventory.get(ids); }
    public static Item get(String... names) { return Inventory.get(names); }
    public static Item get(Predicate<? super Item> filter) { return Inventory.get(filter); }
    public static Query<Item> except(Predicate<? super Item> filter) { return Inventory.except(filter); }
    public static int size() { return Inventory.size(); }
    public static boolean isEmpty() { return Inventory.isEmpty(); }
    public static boolean contains(int id) { return Inventory.contains(id); }
    public static boolean contains(String name) { return Inventory.contains(name); }
    public static boolean contains(int... ids) { return Inventory.contains(ids); }
    public static boolean contains(String... names) { return Inventory.contains(names); }
    public static boolean contains(Predicate<? super Item> filter) { return Inventory.contains(filter); }
    public static boolean contains(Object value) { return Inventory.contains(value); }
    public static boolean containsAll(String... names) { return Inventory.containsAll(names); }
    public static boolean containsAll(int... ids) { return Inventory.containsAll(ids); }
    public static boolean containsAll(Collection<?> values) { return Inventory.containsAll(values); }
    public static boolean isFull() { return Inventory.isFull(); }
    public static int count(String name) { return Inventory.count(name); }
    public static int count(int id) { return Inventory.count(id); }
    public static int count(Predicate<? super Item> filter) { return Inventory.count(filter); }
    public static Object[] toArray() { return Inventory.toArray(); }

    private static boolean clickControl(String text)
    {
        com.dreambotreborn.api.wrappers.widgets.Widget widget = Widgets.find(value ->
            value.groupId == widgetParentId() && value.visible
                && (value.text.toLowerCase().contains(text.toLowerCase())
                    || value.name.toLowerCase().contains(text.toLowerCase())
                    || value.actions.stream().anyMatch(action ->
                        action.toLowerCase().contains(text.toLowerCase())))).first();
        return widget != null && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }
}
