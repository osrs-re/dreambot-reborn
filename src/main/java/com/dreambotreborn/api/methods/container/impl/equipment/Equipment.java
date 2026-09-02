package com.dreambotreborn.api.methods.container.impl.equipment;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.awt.Rectangle;
import java.util.Arrays;
import net.runelite.api.widgets.WidgetInfo;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.ContainerQueries;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.wrappers.widgets.Widget;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.utilities.Await;

/** Worn-equipment queries and interactions. */
public final class Equipment
{
    private static final ContainerType TYPE = ContainerType.EQUIPMENT;

    private Equipment()
    {
    }

    public static int widgetParentId()
    {
        return WidgetInfo.EQUIPMENT.getGroupId();
    }

    public static int widgetChildId()
    {
        return WidgetInfo.EQUIPMENT.getChildId();
    }

    public static int capacity()
    {
        return Containers.capacity(TYPE);
    }

    public static Query<Item> all()
    {
        return ContainerQueries.all(TYPE);
    }

    public static Query<Item> all(Predicate<? super Item> predicate)
    {
        return ContainerQueries.all(TYPE, predicate);
    }

    public static Query<Item> find(Predicate<? super Item> predicate)
    {
        return all(predicate);
    }

    public static boolean isLoaded()
    {
        return Containers.isLoaded(TYPE);
    }

    public static Item getItemInSlot(int slot)
    {
        return ContainerQueries.getInSlot(TYPE, slot);
    }

    public static Item getItemInSlot(EquipmentSlot slot)
    {
        return slot == null ? null : getItemInSlot(slot.getSlot());
    }

    public static Item get(int id)
    {
        return ContainerQueries.get(TYPE, id);
    }

    public static Item get(String name)
    {
        return ContainerQueries.get(TYPE, name);
    }

    public static Item get(Predicate<? super Item> predicate)
    {
        return ContainerQueries.get(TYPE, Objects.requireNonNull(predicate, "predicate"));
    }

    public static boolean contains(int id)
    {
        return get(id) != null;
    }

    public static boolean contains(String name)
    {
        return get(name) != null;
    }

    public static boolean contains(Predicate<? super Item> predicate)
    {
        return ContainerQueries.contains(TYPE, predicate);
    }

    public static boolean containsAll(String... names)
    {
        return ContainerQueries.containsAllNames(TYPE, names);
    }

    public static boolean containsAll(int... ids)
    {
        return ContainerQueries.containsAllIds(TYPE, ids);
    }

    public static boolean containsAll(Collection<?> values)
    {
        return ContainerQueries.containsAll(TYPE, values);
    }

    public static int count(int id)
    {
        return ContainerQueries.count(TYPE, item -> item.id == id);
    }

    public static int count(String name)
    {
        return ContainerQueries.count(TYPE,
            item -> name != null && item.name.equalsIgnoreCase(name));
    }

    public static int count(Predicate<? super Item> predicate)
    {
        return ContainerQueries.count(TYPE, predicate);
    }

    public static int fullSlotCount()
    {
        return ContainerQueries.fullSlots(TYPE);
    }

    public static int emptySlotCount()
    {
        return ContainerQueries.emptySlots(TYPE);
    }

    public static int size()
    {
        return fullSlotCount();
    }

    public static boolean isEmpty()
    {
        return fullSlotCount() == 0;
    }

    public static boolean isFull()
    {
        return emptySlotCount() == 0;
    }

    public static boolean isSlotEmpty(int slot)
    {
        return getItemInSlot(slot) == null;
    }

    public static boolean isSlotEmpty(EquipmentSlot slot)
    {
        return slot == null || isSlotEmpty(slot.getSlot());
    }

    public static boolean isSlotFull(int slot)
    {
        return !isSlotEmpty(slot);
    }

    public static boolean isSlotFull(EquipmentSlot slot)
    {
        return !isSlotEmpty(slot);
    }

    public static int getFirstEmptySlot()
    {
        for (int slot = 0; slot < capacity(); slot++)
        {
            if (isSlotEmpty(slot)) return slot;
        }
        return -1;
    }

    public static int getFirstFullSlot()
    {
        Item item = all().first();
        return item == null ? -1 : item.slot;
    }

    public static int getIdForSlot(int slot)
    {
        Item item = getItemInSlot(slot);
        return item == null ? -1 : item.id;
    }

    public static int getIdForSlot(EquipmentSlot slot)
    {
        return slot == null ? -1 : getIdForSlot(slot.getSlot());
    }

    public static String getNameForSlot(int slot)
    {
        Item item = getItemInSlot(slot);
        return item == null ? null : item.name;
    }

    public static String getNameForSlot(EquipmentSlot slot)
    {
        return slot == null ? null : getNameForSlot(slot.getSlot());
    }

    public static boolean slotContains(int slot, String... names)
    {
        Item item = getItemInSlot(slot);
        return item != null && Queries.name(item.name, names);
    }

    public static boolean slotContains(EquipmentSlot slot, String... names)
    {
        return slot != null && slotContains(slot.getSlot(), names);
    }

    public static boolean slotContains(int slot, int... ids)
    {
        Item item = getItemInSlot(slot);
        return item != null && Queries.id(item.id, ids);
    }

    public static boolean slotContains(EquipmentSlot slot, int... ids)
    {
        return slot != null && slotContains(slot.getSlot(), ids);
    }

    public static boolean slotContains(int slot, Item item)
    {
        Item equipped = getItemInSlot(slot);
        return item != null && equipped != null && equipped.id == item.id;
    }

    public static boolean slotContains(EquipmentSlot slot, Item item)
    {
        return slot != null && slotContains(slot.getSlot(), item);
    }

    public static boolean slotContains(int slot, Predicate<? super Item> predicate)
    {
        Item item = getItemInSlot(slot);
        return item != null && predicate != null && predicate.test(item);
    }

    public static boolean slotContains(
        EquipmentSlot slot, Predicate<? super Item> predicate)
    {
        return slot != null && slotContains(slot.getSlot(), predicate);
    }

    public static boolean slotNameContains(int slot, String text)
    {
        Item item = getItemInSlot(slot);
        return item != null && text != null
            && item.name.toLowerCase().contains(text.toLowerCase());
    }

    public static boolean slotNameContains(EquipmentSlot slot, String text)
    {
        return slot != null && slotNameContains(slot.getSlot(), text);
    }

    public static boolean onlyContains(String... names)
    {
        return ContainerQueries.onlyContains(TYPE, item -> Queries.name(item.name, names));
    }

    public static boolean onlyContains(int... ids)
    {
        return ContainerQueries.onlyContains(TYPE, item -> Queries.id(item.id, ids));
    }

    public static boolean onlyContains(Predicate<? super Item> predicate)
    {
        return ContainerQueries.onlyContains(TYPE, predicate);
    }

    public static EquipmentSlot getSlotForItem(Predicate<? super Item> predicate)
    {
        int index = slot(predicate);
        for (EquipmentSlot value : EquipmentSlot.values())
        {
            if (value.getSlot() == index) return value;
        }
        return null;
    }

    public static Object[] toArray()
    {
        return all().all().toArray();
    }

    public static boolean isOpen()
    {
        return Tabs.isOpen(Tab.EQUIPMENT);
    }

    public static boolean open()
    {
        return Tabs.open(Tab.EQUIPMENT);
    }

    public static Widget getWidgetForSlot(EquipmentSlot slot)
    {
        if (slot == null) return null;
        net.runelite.api.widgets.Widget widget = Containers.widgetForSlot(TYPE, slot.getSlot());
        return widget == null ? null : new Widget(widget);
    }

    public static Rectangle getSlotBounds(EquipmentSlot slot)
    {
        Widget widget = getWidgetForSlot(slot);
        return widget == null ? null : widget.getBounds();
    }

    public static int slot(int id)
    {
        return ContainerQueries.slot(TYPE, item -> item.id == id);
    }

    public static int slot(String name)
    {
        return ContainerQueries.slot(TYPE,
            item -> name != null && item.name.equalsIgnoreCase(name));
    }

    public static int slot(Predicate<? super Item> predicate)
    {
        return ContainerQueries.slot(TYPE, predicate);
    }

    public static boolean interact(EquipmentSlot slot, String action)
    {
        Item item = getItemInSlot(slot);
        return item != null && item.interact(action);
    }

    public static boolean unequip(EquipmentSlot slot)
    {
        return interact(slot, "Remove");
    }

    public static boolean unequip(Predicate<? super Item> predicate)
    {
        Item item = get(predicate);
        return item != null && item.interact("Remove");
    }

    public static boolean equip(EquipmentSlot slot, String... names)
    {
        Item item = Inventory.get(candidate -> Queries.name(candidate.name, names));
        return equipInventoryItem(item);
    }

    public static boolean equip(EquipmentSlot slot, int... ids)
    {
        Item item = Inventory.get(candidate -> Queries.id(candidate.id, ids));
        return equipInventoryItem(item);
    }

    public static boolean equip(
        EquipmentSlot slot, Predicate<? super Item> predicate)
    {
        return equipInventoryItem(Inventory.get(predicate));
    }

    public static CompletableFuture<Boolean> interactAsync(EquipmentSlot slot, String action)
    {
        Item item = getItemInSlot(slot);
        return item == null ? CompletableFuture.completedFuture(false) : item.interactAsync(action);
    }

    public static CompletableFuture<Boolean> unequipAsync(EquipmentSlot slot)
    {
        return interactAsync(slot, "Remove");
    }

    private static boolean equipInventoryItem(Item item)
    {
        if (item == null)
        {
            return false;
        }
        for (String action : new String[] {"Wear", "Wield", "Equip"})
        {
            if (item.hasAction(action))
            {
                return item.interact(action);
            }
        }
        return false;
    }
}
