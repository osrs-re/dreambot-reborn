package com.dreambotreborn.api.methods.container.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.ContainerQueries;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;

/** Inventory queries and visible item interactions. */
public final class Inventory
{
    private static final ContainerType TYPE = ContainerType.INVENTORY;
    private static volatile DropPattern dropPattern = DropPattern.SLOT_ORDER;
    private static volatile boolean forceNoShift;
    private static volatile int selectedWidgetItemId = -1;

    private Inventory()
    {
    }

    public static int widgetParentId()
    {
        return WidgetInfo.INVENTORY.getGroupId();
    }

    public static int widgetChildId()
    {
        return WidgetInfo.INVENTORY.getChildId();
    }

    public static int capacity()
    {
        return Containers.capacity(TYPE);
    }

    public static DropPattern getDropPattern() { return dropPattern; }

    public static void setDropPattern(DropPattern pattern)
    {
        dropPattern = Objects.requireNonNull(pattern, "pattern");
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getInventoryWidget()
    {
        Widget widget = Containers.rootWidget(TYPE);
        return widget == null ? null : new com.dreambotreborn.api.wrappers.widgets.Widget(widget);
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

    public static boolean isOpen()
    {
        Widget widget = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.INVENTORY);
        return widget != null && !widget.isHidden();
    }

    public static boolean open()
    {
        return Tabs.open(Tab.INVENTORY);
    }

    public static Item getItemInSlot(int slot)
    {
        return ContainerQueries.getInSlot(TYPE, slot);
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

    public static Item get(int[] ids)
    {
        return get(item -> Queries.id(item.id, ids));
    }

    public static Item get(Integer... ids)
    {
        return get(Queries.unboxIds(ids));
    }

    public static Item get(String... names)
    {
        return get(item -> Queries.name(item.name, names));
    }

    public static boolean contains(int id)
    {
        return get(id) != null;
    }

    public static boolean contains(String name)
    {
        return get(name) != null;
    }

    public static boolean contains(int[] ids)
    {
        return get(ids) != null;
    }

    public static boolean contains(Integer... ids)
    {
        return contains(Queries.unboxIds(ids));
    }

    public static boolean contains(String... names)
    {
        return get(names) != null;
    }

    public static boolean contains(Predicate<? super Item> predicate)
    {
        return ContainerQueries.contains(TYPE, predicate);
    }

    public static boolean contains(Object value)
    {
        if (value instanceof Item)
        {
            return contains(((Item) value).id);
        }
        if (value instanceof Number)
        {
            return contains(((Number) value).intValue());
        }
        return value instanceof String && contains((String) value);
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
        return count(item -> item.id == id);
    }

    public static int count(String name)
    {
        return count(item -> name != null && item.name.equalsIgnoreCase(name));
    }

    public static int count(Predicate<? super Item> predicate)
    {
        return ContainerQueries.count(TYPE, Objects.requireNonNull(predicate, "predicate"));
    }

    public static int size()
    {
        return fullSlotCount();
    }

    public static int fullSlotCount()
    {
        return ContainerQueries.fullSlots(TYPE);
    }

    public static int emptySlotCount()
    {
        return ContainerQueries.emptySlots(TYPE);
    }

    public static int getEmptySlots()
    {
        return emptySlotCount();
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

    public static boolean isSlotFull(int slot)
    {
        return !isSlotEmpty(slot);
    }

    public static int getFirstEmptySlot()
    {
        for (int slot = 0; slot < capacity(); slot++)
        {
            if (isSlotEmpty(slot))
            {
                return slot;
            }
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

    public static String getNameForSlot(int slot)
    {
        Item item = getItemInSlot(slot);
        return item == null ? null : item.name;
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

    public static boolean slotContains(int slot, int... ids)
    {
        Item item = getItemInSlot(slot);
        return item != null && Queries.id(item.id, ids);
    }

    public static boolean slotContains(int slot, String... names)
    {
        Item item = getItemInSlot(slot);
        return item != null && Queries.name(item.name, names);
    }

    public static boolean slotContains(int slot, Predicate<? super Item> predicate)
    {
        Item item = getItemInSlot(slot);
        return item != null && predicate.test(item);
    }

    public static boolean slotNameContains(int slot, String text)
    {
        Item item = getItemInSlot(slot);
        return item != null && text != null
            && item.name.toLowerCase().contains(text.toLowerCase());
    }

    public static boolean onlyContains(String... names)
    {
        return ContainerQueries.onlyContains(TYPE, item -> Queries.name(item.name, names));
    }

    public static boolean onlyContains(int[] ids)
    {
        return ContainerQueries.onlyContains(TYPE, item -> Queries.id(item.id, ids));
    }

    public static boolean onlyContains(Integer... ids)
    {
        return onlyContains(Queries.unboxIds(ids));
    }

    public static boolean onlyContains(Predicate<? super Item> predicate)
    {
        return ContainerQueries.onlyContains(TYPE, predicate);
    }

    public static Item getRandom(String... names)
    {
        return all(item -> Queries.name(item.name, names)).random();
    }

    public static Item getRandom(int... ids)
    {
        return all(item -> Queries.id(item.id, ids)).random();
    }

    public static Item getRandom(Predicate<? super Item> predicate)
    {
        return all(predicate).random();
    }

    public static Item[] toArray()
    {
        return all().all().toArray(new Item[0]);
    }

    public static Query<Item> except(Predicate<? super Item> predicate)
    {
        Objects.requireNonNull(predicate, "predicate");
        return all(item -> !predicate.test(item));
    }

    public static boolean interact(int id)
    {
        return Await.success(interactAsync(id));
    }

    public static boolean interact(int id, String action)
    {
        return Await.success(interactAsync(id, action));
    }

    public static boolean interact(String name)
    {
        return Await.success(interactAsync(name));
    }

    public static boolean interact(String name, String action)
    {
        return Await.success(interactAsync(name, action));
    }

    public static boolean interact(Predicate<? super Item> predicate)
    {
        return Await.success(interactAsync(predicate));
    }

    public static boolean interact(Predicate<? super Item> predicate, String action)
    {
        return Await.success(interactAsync(predicate, action));
    }

    public static boolean interact(Item item)
    {
        return Await.success(interactAsync(item));
    }

    public static boolean interact(Item item, String action)
    {
        return Await.success(interactAsync(item, action));
    }

    public static CompletableFuture<Boolean> interactAsync(int id)
    {
        return interactAsync(get(id), null);
    }

    public static CompletableFuture<Boolean> interactAsync(int id, String action)
    {
        return interactAsync(get(id), action);
    }

    public static CompletableFuture<Boolean> interactAsync(String name)
    {
        return interactAsync(get(name), null);
    }

    public static CompletableFuture<Boolean> interactAsync(String name, String action)
    {
        return interactAsync(get(name), action);
    }

    public static CompletableFuture<Boolean> interactAsync(Predicate<? super Item> predicate)
    {
        return interactAsync(get(predicate), null);
    }

    public static CompletableFuture<Boolean> interactAsync(
        Predicate<? super Item> predicate, String action)
    {
        return interactAsync(get(predicate), action);
    }

    public static CompletableFuture<Boolean> interactAsync(Item item)
    {
        return interactAsync(item, null);
    }

    public static CompletableFuture<Boolean> interactAsync(Item item, String action)
    {
        if (item == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        return action == null ? item.interactAsync() : item.interactAsync(action);
    }

    public static boolean slotInteract(int slot)
    {
        return interact(getItemInSlot(slot));
    }

    public static boolean slotInteract(int slot, String action)
    {
        return interact(getItemInSlot(slot), action);
    }

    public static boolean drop(int id)
    {
        return interact(id, "Drop");
    }

    public static boolean drop(String name)
    {
        return interact(name, "Drop");
    }

    public static boolean drop(Predicate<? super Item> predicate)
    {
        return interact(predicate, "Drop");
    }

    public static boolean dropAll()
    {
        return dropAll(item -> true);
    }

    public static boolean dropAll(int id)
    {
        return dropAll(item -> item.id == id);
    }

    public static boolean dropAll(String name)
    {
        return dropAll(item -> name != null && item.name.equalsIgnoreCase(name));
    }

    public static boolean dropAll(int[] ids)
    {
        return dropAll(item -> Queries.id(item.id, ids));
    }

    public static boolean dropAll(Integer... ids)
    {
        return dropAll(Queries.unboxIds(ids));
    }

    public static boolean dropAll(String... names)
    {
        return dropAll(item -> Queries.name(item.name, names));
    }

    public static boolean dropAll(Predicate<? super Item> predicate)
    {
        if (predicate == null) return false;
        List<Item> items = new ArrayList<>(all(predicate).all());
        Comparator<Item> comparator = dropPattern == null ? null : dropPattern.getComparator();
        if (comparator != null) items.sort(comparator);
        boolean interacted = false;
        for (Item item : items)
        {
            interacted |= item.interact("Drop");
        }
        return interacted;
    }

    public static boolean dropAllExcept(String... names)
    {
        return dropAll(item -> !Queries.name(item.name, names));
    }

    public static boolean dropAllExcept(int[] ids)
    {
        return dropAll(item -> !Queries.id(item.id, ids));
    }

    public static boolean dropAllExcept(Integer... ids)
    {
        return dropAllExcept(Queries.unboxIds(ids));
    }

    public static boolean dropAllExcept(Predicate<? super Item> keep)
    {
        return keep != null && dropAll(item -> !keep.test(item));
    }

    public static Rectangle itemBounds(Item item)
    {
        return item == null ? null : item.getBounds();
    }

    public static Rectangle slotBounds(int slot)
    {
        Widget widget = Containers.widgetForSlot(TYPE, slot);
        return widget == null || widget.getBounds() == null
            ? Containers.approximateSlotBounds(TYPE, slot)
            : new Rectangle(widget.getBounds());
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getWidgetForSlot(int slot)
    {
        Widget widget = Containers.widgetForSlot(TYPE, slot);
        return widget == null ? null : new com.dreambotreborn.api.wrappers.widgets.WidgetChild(widget);
    }

    public static com.dreambotreborn.api.wrappers.widgets.WidgetChild getWidgetForSlot(
        int slot, String action)
    {
        com.dreambotreborn.api.wrappers.widgets.WidgetChild widget = getWidgetForSlot(slot);
        return widget != null && (action == null || widget.hasAction(action)) ? widget : null;
    }

    public static boolean isItemSelected()
    {
        return DreamBotRebornApi.requireClient().isWidgetSelected()
            && DreamBotRebornApi.requireClient().getSelectedWidget() != null;
    }

    public static String getSelectedItemName()
    {
        Widget widget = DreamBotRebornApi.requireClient().getSelectedWidget();
        return widget == null ? null : com.dreambotreborn.api.wrappers.interactive.Entity.clean(widget.getName());
    }

    public static int getSelectedItemIndex()
    {
        Widget widget = DreamBotRebornApi.requireClient().getSelectedWidget();
        return widget == null ? -1 : widget.getIndex();
    }

    public static int getSelectedItemId()
    {
        Widget widget = DreamBotRebornApi.requireClient().getSelectedWidget();
        if (widget != null && widget.getItemId() >= 0) return widget.getItemId();
        return selectedWidgetItemId;
    }

    public static void setSelectedWidgetItemId(int itemId)
    {
        selectedWidgetItemId = itemId;
    }

    public static boolean deselect()
    {
        Widget selected = DreamBotRebornApi.requireClient().getSelectedWidget();
        return !isItemSelected()
            || (selected != null && new com.dreambotreborn.api.wrappers.widgets.Widget(selected).click());
    }

    public static boolean isForceNoShift() { return forceNoShift; }
    public static void setForceNoShift(boolean value) { forceNoShift = value; }
    public static boolean shouldShift() { return !forceNoShift; }

    public static boolean use(int id)
    {
        return interact(id, "Use");
    }

    public static boolean use(String name)
    {
        return interact(name, "Use");
    }

    public static boolean use(Item item)
    {
        return interact(item, "Use");
    }

    public static boolean combine(String first, String second)
    {
        return combine(get(first), get(second));
    }

    public static boolean combine(int first, int second)
    {
        return combine(get(first), get(second));
    }

    public static boolean combine(Item first, Item second)
    {
        if (first == null || second == null || first.slot == second.slot)
        {
            return false;
        }
        return first.interact("Use") && second.interact();
    }

    public static boolean drag(Predicate<? super Item> predicate, int destinationSlot)
    {
        return drag(get(predicate), destinationSlot);
    }

    public static boolean drag(String name, int destinationSlot)
    {
        return drag(get(name), destinationSlot);
    }

    public static boolean drag(int id, int destinationSlot)
    {
        return drag(get(id), destinationSlot);
    }

    public static boolean drag(Item item, int destinationSlot)
    {
        Rectangle source = itemBounds(item);
        Rectangle destination = slotBounds(destinationSlot);
        return source != null && destination != null
            && com.dreambotreborn.api.input.Mouse.move(source)
            && com.dreambotreborn.api.input.Mouse.drag(destination);
    }

    public static boolean swap(Item first, Item second)
    {
        return first != null && second != null && drag(first, second.slot);
    }

    public static boolean swap(int firstSlot, int secondSlot)
    {
        return swap(getItemInSlot(firstSlot), getItemInSlot(secondSlot));
    }

    public static String[] getActionsForSlot(int slot)
    {
        Item item = getItemInSlot(slot);
        return item == null ? new String[0] : item.actions.toArray(new String[0]);
    }
}
