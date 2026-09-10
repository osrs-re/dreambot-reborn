package com.dreambotreborn.api.methods.input;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.wrappers.items.Item;

/** Legacy mouse-keys dropping helper backed by the virtual keyboard and inventory API. */
public final class MouseKeys
{
    private static volatile int jumpDistance = 40;
    private static volatile int minimumSleep = 20;
    private static volatile int maximumSleep = 45;
    private MouseKeys() { }
    public static void setYJumpDistance(int value) { jumpDistance = Math.max(1, value); }
    public static int getJumpDistance() { return jumpDistance; }
    public static boolean setKeySleeps(int minimum, int maximum)
    {
        if (minimum < 0 || maximum < minimum) return false;
        minimumSleep = minimum; maximumSleep = maximum; return true;
    }
    public static boolean dropAllItems() { return Inventory.dropAll(); }
    public static boolean dropAllItemsExcept(int[] ids)
    { return Inventory.dropAllExcept(ids == null ? new int[0] : ids); }
    public static boolean dropAllSpecificItems(int[] ids)
    { return Inventory.dropAll(ids == null ? new int[0] : ids); }
    public static boolean dropAllItemsInSlots(int[] slots)
    { return dropSlots(slots, null, false); }
    public static boolean dropAllItemsExceptInSlots(int[] excluded, int[] included)
    { return dropSlots(included, item -> !contains(excluded, item.getSlot()), false); }
    public static boolean dropAllSpecificItemsInSlots(int[] ids, int[] slots)
    { return dropSlots(slots, item -> contains(ids, item.getId()), false); }
    public static boolean dropItems(Filter<Item> filter)
    { return filter != null && Inventory.dropAll(filter::match); }
    public static boolean dropColumn(int column, Filter<Item> filter)
    {
        if (column < 0 || column > 3) return false;
        boolean changed = false;
        for (int slot = column; slot < Inventory.capacity(); slot += 4)
        {
            Item item = Inventory.getItemInSlot(slot);
            if (item != null && (filter == null || filter.match(item))) changed |= item.interact("Drop");
        }
        return changed;
    }
    public static boolean initialColumnMouseCheck(int column, Filter<Item> filter)
    {
        if (column < 0 || column > 3) return false;
        for (int slot = column; slot < Inventory.capacity(); slot += 4)
        {
            Item item = Inventory.getItemInSlot(slot);
            if (item != null && (filter == null || filter.match(item))) return true;
        }
        return false;
    }
    public static boolean dropItem(Item item, Filter<Item> filter)
    { return item != null && (filter == null || filter.match(item)) && item.interact("Drop"); }
    public static boolean tabCheck() { return Inventory.isOpen() || Inventory.open(); }
    public static boolean skipItem() { return pushFive(); }
    public static boolean clickAndSkipItem()
    {
        Item item = Inventory.all().first();
        return item != null && item.interact("Drop") && skipItem();
    }
    public static boolean mouseKeyCombo() { return pushFive(); }
    public static boolean pushOne() { return push(KeyEvent.VK_NUMPAD1); }
    public static boolean pushTwo() { return push(KeyEvent.VK_NUMPAD2); }
    public static boolean pushThree() { return push(KeyEvent.VK_NUMPAD3); }
    public static boolean pushFour() { return push(KeyEvent.VK_NUMPAD4); }
    public static boolean pushFive() { return push(KeyEvent.VK_NUMPAD5); }
    public static boolean pushSix() { return push(KeyEvent.VK_NUMPAD6); }
    public static boolean pushSeven() { return push(KeyEvent.VK_NUMPAD7); }
    public static boolean pushEight() { return push(KeyEvent.VK_NUMPAD8); }
    public static boolean pushNine() { return push(KeyEvent.VK_NUMPAD9); }
    private static boolean push(int key)
    {
        Keyboard.typeSpecialKey(key);
        if (maximumSleep > 0) com.dreambotreborn.api.utilities.Sleep.sleep(minimumSleep, maximumSleep);
        return true;
    }
    private static boolean dropSlots(int[] slots, Filter<Item> filter, boolean ignored)
    {
        if (slots == null) return false;
        Set<Integer> wanted = new HashSet<>();
        for (int slot : slots) wanted.add(slot);
        boolean changed = false;
        for (Item item : Inventory.all())
            if (wanted.contains(item.getSlot()) && (filter == null || filter.match(item)))
                changed |= item.interact("Drop");
        return changed;
    }
    private static boolean contains(int[] values, int expected)
    {
        if (values != null) for (int value : values) if (value == expected) return true;
        return false;
    }
}
