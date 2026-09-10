package com.dreambotreborn.api.methods.container.impl.bank;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.Varbits;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.ContainerQueries;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.wrappers.interactive.Interactable;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.methods.input.Keyboard;
import com.dreambotreborn.api.input.Mouse;

/** Bank discovery, queries, and visible withdraw/deposit operations. */
public final class Bank
{
    private static final ContainerType TYPE = ContainerType.BANK;
    private static volatile List<Item> bankHistoryCache = Collections.emptyList();
    private static volatile long lastBankHistoryCacheTime;
    private static volatile long lastBankHistoryCacheTick;
    private static volatile boolean useBankHistoryCache = true;
    private static volatile boolean alwaysOpenTab;

    private Bank()
    {
    }

    public static int widgetParentId() { return WidgetInfo.BANK_CONTAINER.getGroupId(); }
    public static int widgetChildId() { return WidgetInfo.BANK_CONTAINER.getChildId(); }
    public static int tabContainerWidgetId() { return WidgetInfo.BANK_TAB_CONTAINER.getChildId(); }

    /** Returns the nearest booth/chest/banker with a Bank action. */
    public static Interactable closest()
    {
        GameObject object = GameObjects.closest(Bank::isBankObject);
        NPC npc = NPCs.closest(candidate -> candidate.hasAction("Bank"));
        if (object == null)
        {
            return npc;
        }
        if (npc == null)
        {
            return object;
        }
        return object.distance() <= npc.distance() ? object : npc;
    }

    /** DreamBot-compatible alias for {@link #closest()}. */
    public static Interactable getClosestBank()
    {
        return closest();
    }

    public static com.dreambotreborn.api.wrappers.interactive.Entity getClosestBank(BankType type)
    {
        if (type == BankType.NPC)
        {
            return closestBanker();
        }
        if (type == BankType.EXCHANGE)
        {
            return GameObjects.closest(object -> object.hasAction("Exchange"));
        }
        String[] actions = type == null ? new String[] {"Bank"} : type.getActions();
        return GameObjects.closest(object -> object.hasAction(actions));
    }

    public static GameObject closestObject()
    {
        return GameObjects.closest(Bank::isBankObject);
    }

    public static NPC closestBanker()
    {
        return NPCs.closest(candidate -> candidate.hasAction("Bank"));
    }

    public static boolean open()
    {
        return Await.success(openAsync());
    }

    public static boolean open(BankLocation location)
    {
        if (location == null || BankLocation.isBlacklisted(location)) return false;
        if (isOpen()) return true;
        if (location.getTile().distance() > 8)
            return com.dreambotreborn.api.methods.walking.impl.Walking.walk(location.getTile());
        com.dreambotreborn.api.wrappers.interactive.Entity bank = getClosestBank(location.getBankType());
        return bank != null && bank.interact(location.getBankType().getActions()[0]);
    }

    public static BankLocation getClosestBankLocation()
    {
        return BankLocation.getNearest();
    }

    public static BankLocation getClosestBankLocation(boolean membersOnly)
    {
        com.dreambotreborn.api.wrappers.interactive.Player player =
            com.dreambotreborn.api.methods.interactive.Players.getLocal();
        return BankLocation.getNearest(player == null ? null : player.getTile(), membersOnly);
    }

    public static CompletableFuture<Boolean> openAsync()
    {
        Interactable bank = closest();
        if (bank == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        return bank.hasAction("Bank") ? bank.interactAsync("Bank") : bank.interactAsync();
    }

    public static boolean isOpen()
    {
        Widget widget = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_CONTAINER);
        return widget != null && !widget.isHidden();
    }

    public static boolean isLoaded()
    {
        return Containers.isLoaded(TYPE);
    }

    public static void setUseBankHistoryCache(boolean value) { useBankHistoryCache = value; }
    public static boolean isUseBankHistoryCache() { return useBankHistoryCache; }

    public static void resetCache()
    {
        bankHistoryCache = Collections.emptyList();
        lastBankHistoryCacheTime = 0L;
        lastBankHistoryCacheTick = 0L;
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

    public static boolean contains(Object value)
    {
        if (value instanceof Item) return contains(((Item) value).id);
        if (value instanceof Number) return contains(((Number) value).intValue());
        return value instanceof String && contains((String) value);
    }

    public static boolean contains(Predicate<? super Item> predicate)
    {
        return ContainerQueries.contains(TYPE, predicate);
    }

    public static boolean containsAll(String... names)
    {
        return ContainerQueries.containsAllNames(TYPE, names);
    }

    public static boolean containsAll(int[] ids)
    {
        return ContainerQueries.containsAllIds(TYPE, ids);
    }

    public static boolean containsAll(Integer... ids)
    {
        return containsAll(Queries.unboxIds(ids));
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

    public static int count(String... names)
    {
        return ContainerQueries.count(TYPE, item -> Queries.name(item.name, names));
    }

    public static int count(int[] ids)
    {
        return ContainerQueries.count(TYPE, item -> Queries.id(item.id, ids));
    }

    public static int count(Integer... ids)
    {
        return count(Queries.unboxIds(ids));
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
        return capacity() > 0 && emptySlotCount() == 0;
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

    public static int getSlot(int id) { return slot(id); }
    public static int getSlot(String name) { return slot(name); }
    public static int getSlot(Predicate<? super Item> predicate) { return slot(predicate); }

    public static Rectangle calculateSlotPosition(int slot) { return slotBounds(slot); }

    public static Rectangle slotBounds(int slot)
    {
        Widget widget = Containers.widgetForSlot(TYPE, slot);
        return widget == null || widget.getBounds() == null
            ? Containers.approximateSlotBounds(TYPE, slot)
            : new Rectangle(widget.getBounds());
    }

    public static Rectangle slotBounds(Item item)
    {
        return item == null ? null : slotBounds(item.slot);
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getChildForSlot(int slot)
    {
        Widget widget = Containers.widgetForSlot(TYPE, slot);
        return widget == null ? null : new com.dreambotreborn.api.wrappers.widgets.Widget(widget);
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getChild(int id)
    {
        Item item = get(id);
        return item == null ? null : getChildForSlot(item.slot);
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getChild(String name)
    {
        Item item = get(name);
        return item == null ? null : getChildForSlot(item.slot);
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getChild(
        Predicate<? super Item> predicate)
    {
        Item item = get(predicate);
        return item == null ? null : getChildForSlot(item.slot);
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getChild(Item item)
    {
        return item == null ? null : getChildForSlot(item.slot);
    }

    public static boolean withdraw(int id) { return Await.success(withdrawAsync(id)); }
    public static boolean withdraw(String name) { return Await.success(withdrawAsync(name)); }
    public static boolean withdraw(int id, int amount)
    {
        return Await.success(withdrawAsync(id, amount));
    }
    public static boolean withdraw(String name, int amount)
    {
        return Await.success(withdrawAsync(name, amount));
    }
    public static boolean withdraw(Predicate<? super Item> predicate, int amount)
    {
        return Await.success(withdrawAsync(predicate, amount));
    }
    public static boolean withdrawAll(int id) { return Await.success(withdrawAllAsync(id)); }
    public static boolean withdrawAll(String name)
    {
        return Await.success(withdrawAllAsync(name));
    }
    public static boolean withdrawAll(Predicate<? super Item> predicate)
    {
        return Await.success(withdrawAllAsync(predicate));
    }
    public static boolean deposit(int id) { return Await.success(depositAsync(id)); }
    public static boolean deposit(String name) { return Await.success(depositAsync(name)); }
    public static boolean deposit(int id, int amount)
    {
        return Await.success(depositAsync(id, amount));
    }
    public static boolean deposit(String name, int amount)
    {
        return Await.success(depositAsync(name, amount));
    }
    public static boolean deposit(Item item, int amount)
    {
        return Await.success(depositAsync(item, amount));
    }
    public static boolean deposit(Item item) { return deposit(item, 1); }
    public static boolean deposit(Predicate<? super Item> predicate)
    {
        return deposit(predicate, 1);
    }
    public static boolean deposit(Predicate<? super Item> predicate, int amount)
    {
        return deposit(Inventory.get(predicate), amount);
    }
    public static boolean depositAll(int id) { return Await.success(depositAllAsync(id)); }
    public static boolean depositAll(String name)
    {
        return Await.success(depositAllAsync(name));
    }
    public static boolean depositAll(Item item)
    {
        return deposit(item, Integer.MAX_VALUE);
    }
    public static boolean depositAll(Predicate<? super Item> predicate)
    {
        Item item = Inventory.get(predicate);
        return item != null && depositAll(item);
    }

    public static boolean depositAllItems()
    {
        Widget widget = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);
        return isOpen() && widget != null
            && new com.dreambotreborn.api.wrappers.widgets.Widget(widget).click();
    }

    public static boolean depositAllEquipment()
    {
        Widget widget = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_DEPOSIT_EQUIPMENT);
        return isOpen() && widget != null
            && new com.dreambotreborn.api.wrappers.widgets.Widget(widget).click();
    }

    public static boolean depositAllExcept(String... names)
    {
        return depositAllExcept(item -> Queries.name(item.name, names));
    }

    public static boolean depositAllExcept(int[] ids)
    {
        return depositAllExcept(item -> Queries.id(item.id, ids));
    }

    public static boolean depositAllExcept(Integer... ids)
    {
        return depositAllExcept(Queries.unboxIds(ids));
    }

    public static boolean depositAllExcept(Predicate<? super Item> keep)
    {
        if (!isOpen() || keep == null) return false;
        boolean changed = false;
        for (Item item : Inventory.all(candidate -> !keep.test(candidate)))
        {
            changed |= depositAll(item);
        }
        return changed;
    }

    public static boolean close()
    {
        if (!isOpen()) return true;
        Keyboard.pressEsc();
        return true;
    }

    public static CompletableFuture<Boolean> withdrawAsync(int id)
    {
        return withdrawAsync(id, 1);
    }

    public static CompletableFuture<Boolean> withdrawAsync(String name)
    {
        return withdrawAsync(name, 1);
    }

    public static CompletableFuture<Boolean> withdrawAsync(int id, int amount)
    {
        return withdrawAsync(get(id), amount);
    }

    public static CompletableFuture<Boolean> withdrawAsync(String name, int amount)
    {
        return withdrawAsync(get(name), amount);
    }

    public static CompletableFuture<Boolean> withdrawAsync(
        Predicate<? super Item> predicate, int amount)
    {
        return withdrawAsync(get(predicate), amount);
    }

    public static CompletableFuture<Boolean> withdrawAllAsync(int id)
    {
        return withdrawAsync(get(id), Integer.MAX_VALUE);
    }

    public static CompletableFuture<Boolean> withdrawAllAsync(String name)
    {
        return withdrawAsync(get(name), Integer.MAX_VALUE);
    }

    public static CompletableFuture<Boolean> withdrawAllAsync(Predicate<? super Item> predicate)
    {
        return withdrawAsync(get(predicate), Integer.MAX_VALUE);
    }

    public static CompletableFuture<Boolean> depositAsync(int id)
    {
        return depositAsync(id, 1);
    }

    public static CompletableFuture<Boolean> depositAsync(String name)
    {
        return depositAsync(name, 1);
    }

    public static CompletableFuture<Boolean> depositAsync(int id, int amount)
    {
        return depositAsync(Inventory.get(id), amount);
    }

    public static CompletableFuture<Boolean> depositAsync(String name, int amount)
    {
        return depositAsync(Inventory.get(name), amount);
    }

    public static CompletableFuture<Boolean> depositAsync(Item item, int amount)
    {
        return interactQuantity(item, "Deposit", amount);
    }

    public static CompletableFuture<Boolean> depositAllAsync(int id)
    {
        return depositAsync(Inventory.get(id), Integer.MAX_VALUE);
    }

    public static CompletableFuture<Boolean> depositAllAsync(String name)
    {
        return depositAsync(Inventory.get(name), Integer.MAX_VALUE);
    }

    private static CompletableFuture<Boolean> withdrawAsync(Item item, int amount)
    {
        return interactQuantity(item, "Withdraw", amount);
    }

    private static CompletableFuture<Boolean> interactQuantity(Item item, String verb, int amount)
    {
        if (item == null)
        {
            return CompletableFuture.completedFuture(false);
        }
        String suffix = amount == Integer.MAX_VALUE ? "All" : Integer.toString(amount);
        String exact = verb + '-' + suffix;
        if (item.hasAction(exact))
        {
            return item.interactAsync(exact);
        }
        for (String action : item.actions)
        {
            if (action.equalsIgnoreCase(exact)
                || (amount == 1 && action.equalsIgnoreCase(verb + "-1")))
            {
                return item.interactAsync(action);
            }
        }
        String xAction = verb + "-X";
        if (amount > 0 && amount != Integer.MAX_VALUE && item.hasAction(xAction))
        {
            return item.interactAsync(xAction).thenCompose(interacted -> interacted
                ? com.dreambotreborn.api.input.Keyboard.type(amount, true)
                : CompletableFuture.completedFuture(false));
        }
        return CompletableFuture.completedFuture(false);
    }

    public static int getCurrentTab()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.CURRENT_BANK_TAB);
    }

    public static BankTab getCurrentBankTab()
    {
        int current = getCurrentTab();
        for (BankTab tab : BankTab.values()) if (tab.getId() == current) return tab;
        return null;
    }

    public static boolean openTab(int tab) { return openTab(tabForId(tab)); }

    public static boolean openTab(BankTab tab)
    {
        if (!isOpen() || tab == null) return false;
        if (tab.isOpen()) return true;
        com.dreambotreborn.api.wrappers.widgets.Widget widget = getTabWidget(tab);
        return widget != null && (widget.getActions().isEmpty()
            ? widget.click() : widget.interact());
    }

    public static com.dreambotreborn.api.wrappers.widgets.Widget getTabWidget(BankTab tab)
    {
        if (tab == null) return null;
        String number = Integer.toString(tab.getId());
        return Widgets.find(widget -> widget.groupId == widgetParentId() && widget.visible
            && (containsIgnoreCase(widget.text, tab == BankTab.MAIN_TAB ? "all items" : "tab " + number)
                || anyContains(widget.actions,
                    tab == BankTab.MAIN_TAB ? "view all" : "tab " + number))).first();
    }

    public static BankMode getRearrangeMode()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.BANK_REARRANGE_MODE) == 1
            ? BankMode.INSERT : BankMode.SWAP;
    }

    public static BankMode getWithdrawMode()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.BANK_ITEM_OPTIONS) == 1
            ? BankMode.NOTE : BankMode.ITEM;
    }

    public static boolean setWithdrawMode(BankMode mode)
    {
        if (mode != BankMode.ITEM && mode != BankMode.NOTE) return false;
        return getWithdrawMode() == mode || clickBankControl(mode.toString());
    }

    public static boolean setRearrangeMode(BankMode mode)
    {
        if (mode != BankMode.SWAP && mode != BankMode.INSERT) return false;
        return getRearrangeMode() == mode || clickBankControl(mode.toString());
    }

    public static BankQuantitySelection getDefaultQuantity()
    {
        return BankQuantitySelection.getSelection();
    }

    public static boolean setDefaultQuantity(BankQuantitySelection selection)
    {
        return selection != null && (getDefaultQuantity() == selection
            || clickBankControl(selection.toString()));
    }

    public static boolean placeHoldersEnabled()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.BANK_LEAVEPLACEHOLDERS) == 1;
    }

    public static boolean togglePlaceholders(boolean enabled)
    {
        return placeHoldersEnabled() == enabled || clickBankControl("Placeholder");
    }

    public static boolean isSlotVisible(Item item)
    {
        Rectangle bounds = slotBounds(item);
        Widget container = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return bounds != null && container != null && container.getBounds() != null
            && container.getBounds().intersects(bounds);
    }

    public static boolean needToScrollUp(Item item)
    {
        Rectangle bounds = slotBounds(item);
        Widget container = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return bounds != null && container != null && container.getBounds() != null
            && bounds.y < container.getBounds().y;
    }

    public static boolean needToScrollDown(Item item)
    {
        Rectangle bounds = slotBounds(item);
        Widget container = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return bounds != null && container != null && container.getBounds() != null
            && bounds.y + bounds.height > container.getBounds().y + container.getBounds().height;
    }

    public static boolean needToScroll(Item item)
    {
        return item != null && !isSlotVisible(item);
    }

    public static int getRow(Item item) { return item == null ? -1 : item.slot / 8; }
    public static int getTab(Item item) { return item == null ? -1 : getTab(item.slot); }

    public static int getTab(int slot)
    {
        if (slot < 0) return -1;
        int cursor = 0;
        for (int tab = 1; tab <= 9; tab++)
        {
            int count = DreamBotRebornApi.requireClient().getVarbitValue(4170 + tab);
            if (slot < cursor + count) return tab;
            cursor += count;
        }
        return 0;
    }

    public static int availableTabs()
    {
        int available = 0;
        for (int tab = 1; tab <= 9; tab++)
        {
            if (DreamBotRebornApi.requireClient().getVarbitValue(4170 + tab) > 0) available = tab;
        }
        return available;
    }

    public static int count(BankTab tab)
    {
        if (tab == null) return 0;
        if (tab != BankTab.MAIN_TAB)
        {
            return DreamBotRebornApi.requireClient().getVarbitValue(tab.getConfig());
        }
        int count = fullSlotCount();
        for (int index = 1; index <= 9; index++)
            count -= DreamBotRebornApi.requireClient().getVarbitValue(4170 + index);
        return Math.max(0, count);
    }

    public static int getScrollHeight()
    {
        Widget widget = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return widget == null ? 0 : widget.getScrollHeight();
    }

    public static boolean scroll(int id) { return scroll(item -> item.id == id); }
    public static boolean scroll(String name)
    {
        return scroll(item -> name != null && item.name.equalsIgnoreCase(name));
    }
    public static boolean scroll(Predicate<? super Item> predicate)
    {
        return scroll(predicate, BankScroll.WHEEL);
    }
    public static boolean scroll(int id, BankScroll method)
    {
        return scroll(item -> item.id == id, method);
    }
    public static boolean scroll(String name, BankScroll method)
    {
        return scroll(item -> name != null && item.name.equalsIgnoreCase(name), method);
    }
    public static boolean scroll(Predicate<? super Item> predicate, BankScroll method)
    {
        Item item = get(predicate);
        Widget container = DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (item == null || container == null || container.getBounds() == null) return false;
        if (isSlotVisible(item)) return true;
        Rectangle bounds = container.getBounds();
        return Mouse.move(bounds) && Mouse.scroll(needToScrollDown(item), 8,
            () -> isSlotVisible(item));
    }

    public static List<Item> getBankHistoryCache() { return bankHistoryCache; }
    public static boolean addToBankHistoryCache(Item item)
    {
        if (item == null) return false;
        List<Item> copy = new ArrayList<>(bankHistoryCache);
        copy.add(item);
        bankHistoryCache = Collections.unmodifiableList(copy);
        lastBankHistoryCacheTime = System.currentTimeMillis();
        return true;
    }
    public static long getLastBankHistoryCacheTime() { return lastBankHistoryCacheTime; }
    public static long getLastBankHistoryCacheTick() { return lastBankHistoryCacheTick; }
    public static boolean isCached() { return !bankHistoryCache.isEmpty(); }
    public static void updateCache(Collection<Item> items)
    {
        bankHistoryCache = Collections.unmodifiableList(new ArrayList<>(items));
        lastBankHistoryCacheTime = System.currentTimeMillis();
        lastBankHistoryCacheTick++;
    }
    public static void updateCache() { updateCache(all().all()); }

    public static boolean drag(int sourceSlot, int destinationSlot)
    {
        Rectangle source = slotBounds(sourceSlot);
        Rectangle destination = slotBounds(destinationSlot);
        return source != null && destination != null && Mouse.move(source) && Mouse.drag(destination);
    }

    public static int getCustomWithdrawAmount()
    {
        return DreamBotRebornApi.requireClient().getVarbitValue(Varbits.BANK_REQUESTEDQUANTITY);
    }
    public static boolean isAlwaysOpenTab() { return alwaysOpenTab; }
    public static void setAlwaysOpenTab(boolean value) { alwaysOpenTab = value; }

    private static boolean clickBankControl(String text)
    {
        com.dreambotreborn.api.wrappers.widgets.Widget widget = Widgets.find(value ->
            value.groupId == widgetParentId() && value.visible
                && (containsIgnoreCase(value.text, text)
                    || containsIgnoreCase(value.name, text)
                    || anyContains(value.actions, text))).first();
        return widget != null && (widget.actions.isEmpty() ? widget.click() : widget.interact());
    }

    private static boolean anyContains(List<String> values, String needle)
    {
        for (String value : values) if (containsIgnoreCase(value, needle)) return true;
        return false;
    }

    private static boolean containsIgnoreCase(String value, String needle)
    {
        return value != null && needle != null
            && value.toLowerCase().contains(needle.toLowerCase());
    }

    private static BankTab tabForId(int id)
    {
        for (BankTab tab : BankTab.values()) if (tab.getId() == id) return tab;
        return null;
    }

    private static boolean isBankObject(GameObject object)
    {
        return object.hasAction("Bank")
            || (object.name.toLowerCase().contains("bank")
                && (object.hasAction("Use") || object.hasAction("Open")));
    }
}
