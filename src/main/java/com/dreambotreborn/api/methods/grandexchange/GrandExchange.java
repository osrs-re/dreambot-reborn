package com.dreambotreborn.api.methods.grandexchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.interactive.NPCs;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.widgets.WidgetInfo;

/** Grand Exchange state and controls backed by the live client and visible widgets. */
public final class GrandExchange
{
    private static volatile int lastChosenItemId = -1;

    private GrandExchange() { }

    public static boolean open(String name)
    {
        com.dreambotreborn.api.wrappers.interactive.NPC npc = NPCs.closest(value ->
            (name == null || value.name.equalsIgnoreCase(name)) && value.hasAction("Exchange"));
        if (npc != null) return npc.interact("Exchange");
        return open();
    }

    public static boolean open()
    {
        if (isOpen()) return true;
        com.dreambotreborn.api.wrappers.interactive.NPC npc =
            NPCs.closest(value -> value.hasAction("Exchange"));
        if (npc != null) return npc.interact("Exchange");
        com.dreambotreborn.api.wrappers.interactive.GameObject object =
            GameObjects.closest(value -> value.hasAction("Exchange"));
        return object != null && object.interact("Exchange");
    }

    public static boolean isOpen()
    {
        return visible(WidgetInfo.GRAND_EXCHANGE_WINDOW_CONTAINER);
    }

    public static boolean isGeneralOpen() { return isOpen() && !isBuyOpen() && !isSellOpen(); }
    public static boolean isSellOpen() { return isOpen() && visibleText("sell offer"); }
    public static boolean isBuyOpen() { return isOpen() && visibleText("buy offer"); }
    public static boolean isSearchOpen() { return isOpen() && visibleText("search"); }
    public static int getOpenSlot()
    {
        for (WidgetChild widget : Widgets.getAll(value -> value.visible && value.hasAction("Abort offer")))
        {
            int index = widget.index;
            if (index >= 0 && index < 8) return index;
        }
        return -1;
    }
    public static String getOfferText()
    {
        net.runelite.api.widgets.Widget widget =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.GRAND_EXCHANGE_OFFER_TEXT);
        return widget == null || widget.getText() == null ? "" : clean(widget.getText());
    }
    public static boolean openSellScreen(int slot) { return clickOfferAction(slot, "Sell"); }
    public static boolean openBuyScreen(int slot) { return clickOfferAction(slot, "Buy"); }
    public static boolean openSlotInterface(int slot)
    {
        if (slot < 0 || slot >= getItems().length) return false;
        GrandExchangeItem item = getItems()[slot];
        if (item.getStatus() == Status.EMPTY) return false;
        return clickNth(value -> value.visible && (value.hasAction("View offer")
            || value.hasAction("Abort offer")), slot);
    }
    public static boolean addSellItem(String name)
    {
        Item item = com.dreambotreborn.api.methods.container.impl.Inventory.get(name);
        return addSellItem(item);
    }
    public static boolean addSellItem(Item item)
    {
        if (item == null) return false;
        lastChosenItemId = item.getId();
        return item.hasAction("Offer") ? item.interact("Offer") : item.interact();
    }
    public static boolean searchItem(String name)
    {
        if (name == null || name.trim().isEmpty()) return false;
        WidgetChild search = first(value -> value.visible && (value.hasAction("Search")
            || value.text.equalsIgnoreCase("Search")));
        if (search != null && !(search.hasAction("Search") ? search.interact("Search") : search.click()))
            return false;
        return Await.success(com.dreambotreborn.api.input.Keyboard.type(name, false));
    }
    public static String getSearchedItem()
    {
        WidgetChild field = first(value -> value.visible && value.text.toLowerCase(Locale.ENGLISH)
            .startsWith("searching for"));
        return field == null ? "" : field.text.replaceFirst("(?i)^searching for[: ]*", "");
    }
    public static boolean addBuyItem(String name)
    {
        if (!searchItem(name)) return false;
        WidgetChild item = getItemChildInSearch(name);
        if (item == null) return false;
        lastChosenItemId = item.itemId;
        return item.hasAction("Select") ? item.interact("Select") : item.click();
    }
    public static boolean addBuyItem(Item item) { return item != null && addBuyItem(item.getName()); }
    public static boolean itemVisible(WidgetChild item) { return item != null && item.isVisible(); }
    public static boolean scrollToItem(WidgetChild item) { return itemVisible(item); }
    public static WidgetChild getItemChildInSearch(String name) { return getItemChildInSearch(name, -1); }
    public static WidgetChild getItemChildInSearch(String name, int id)
    {
        if (name == null && id < 0) return null;
        return first(value -> value.visible && (id < 0 || value.itemId == id)
            && (name == null || clean(value.name + " " + value.text)
                .toLowerCase(Locale.ENGLISH).contains(name.toLowerCase(Locale.ENGLISH))));
    }
    public static boolean setQuantity(int amount)
    {
        if (amount <= 0) return false;
        WidgetChild button = getEnterQuantityButton();
        return click(button) && Await.success(com.dreambotreborn.api.input.Keyboard.type(amount, true));
    }
    public static boolean setPrice(int price)
    {
        if (price <= 0) return false;
        WidgetChild button = getEnterPriceButton();
        return click(button) && Await.success(com.dreambotreborn.api.input.Keyboard.type(price, true));
    }
    public static boolean readyToEnterAmount() { return getEnterQuantityButton() != null; }
    public static boolean readyToEnterPrice() { return getEnterPriceButton() != null; }
    public static WidgetChild getOfferFirstItemWidget() { return firstItemWidget(0); }
    public static WidgetChild getOfferSecondItemWidget() { return firstItemWidget(1); }
    public static WidgetChild getEnterQuantityButton() { return button("Enter quantity"); }
    public static WidgetChild getEnterAllButton() { return button("All"); }
    public static int getCurrentPrice() { return valueNear("price"); }
    public static int getCurrentAmount() { return valueNear("quantity"); }
    public static WidgetChild getEnterPriceButton() { return button("Enter price"); }
    public static WidgetChild getGuidePriceButton() { return button("Guide price"); }
    public static WidgetChild getDecreasePriceFivePercentButton() { return button("-5%"); }
    public static WidgetChild getIncreasePriceFivePercentButton() { return button("+5%"); }
    public static WidgetChild getDecreasePriceXPercentButton() { return button("Decrease price"); }
    public static WidgetChild getIncreasePriceXPercentButton() { return button("Increase price"); }
    public static boolean close() { WidgetChild child = getCloseButton(); return child != null && click(child); }
    public static WidgetChild getCloseButton() { return button("Close"); }
    public static boolean sellItem(String name, int amount, int price)
    {
        int slot = getFirstOpenSlot();
        return slot >= 0 && openSellScreen(slot) && addSellItem(name)
            && setQuantity(amount) && setPrice(price) && confirm();
    }
    public static boolean sellItem(int id, int amount, int price)
    {
        Item item = com.dreambotreborn.api.methods.container.impl.Inventory.get(id);
        return item != null && sellItem(item.getName(), amount, price);
    }
    public static boolean buyItem(String name, int amount, int price)
    {
        int slot = getFirstOpenSlot();
        return slot >= 0 && openBuyScreen(slot) && addBuyItem(name)
            && setQuantity(amount) && setPrice(price) && confirm();
    }
    public static boolean buyItem(int id, int amount, int price)
    {
        net.runelite.api.ItemComposition definition =
            DreamBotRebornApi.requireClient().getItemDefinition(id);
        return definition != null && buyItem(definition.getName(), amount, price);
    }
    public static boolean confirm() { return click(button("Confirm")); }
    public static boolean goBack() { return click(button("Back")); }
    public static boolean cancelOffer(int slot)
    {
        return openSlotInterface(slot) && click(button("Abort offer"));
    }
    public static boolean cancelAll()
    {
        boolean result = false;
        for (int slot = 0; slot < getItems().length; slot++)
            if (slotContainsItem(slot)) result |= cancelOffer(slot);
        return result;
    }
    public static boolean collect() { return click(button("Collect")); }
    public static boolean collectToBank() { return click(button("Collect to bank")); }
    public static boolean isReadyToCollect(int slot)
    {
        return slot >= 0 && slot < getItems().length && getItems()[slot].isReadyToCollect();
    }
    public static boolean isReadyToCollect()
    {
        for (GrandExchangeItem item : getItems()) if (item.isReadyToCollect()) return true;
        return false;
    }
    public static boolean slotContainsItem(int slot)
    {
        return slot >= 0 && slot < getItems().length && getItems()[slot].getStatus() != Status.EMPTY;
    }
    public static int getFirstOpenSlot()
    {
        for (int slot = 0; slot < getItems().length; slot++) if (!slotContainsItem(slot)) return slot;
        return -1;
    }
    public static boolean isSlotEnabled(int slot) { return slot >= 0 && slot < getItems().length; }
    public static GrandExchangeItem[] getItems()
    {
        GrandExchangeOffer[] offers = DreamBotRebornApi.requireClient().getGrandExchangeOffers();
        if (offers == null) return new GrandExchangeItem[0];
        GrandExchangeItem[] result = new GrandExchangeItem[offers.length];
        for (int i = 0; i < offers.length; i++) result[i] = new GrandExchangeItem(offers[i], i);
        return result;
    }
    public static int getCurrentChosenItemId()
    {
        int id = DreamBotRebornApi.requireClient().getVarpValue(net.runelite.api.VarPlayer.CURRENT_GE_ITEM);
        if (id > 0) lastChosenItemId = id;
        return id;
    }
    public static int getCurrentChosenItemID() { return getCurrentChosenItemId(); }
    public static int getLastChosenItemId() { return lastChosenItemId; }
    public static int getLastChosenItemID() { return getLastChosenItemId(); }
    public static Item getCurrentChosenItem() { return item(getCurrentChosenItemId()); }
    public static Item getLastChosenItem() { return item(lastChosenItemId); }
    public static int getUsedSlots()
    {
        int count = 0; for (GrandExchangeItem item : getItems()) if (item.getStatus() != Status.EMPTY) count++;
        return count;
    }
    public static int getOpenSlots() { return Math.max(0, getItems().length - getUsedSlots()); }
    public static boolean contains(Integer... ids) { return getItem(ids) != null; }
    public static boolean contains(int[] ids) { return getItem(ids) != null; }
    public static boolean contains(String... names) { return getItem(names) != null; }
    public static boolean contains(Filter<Item> filter) { return getItem(filter) != null; }
    public static GrandExchangeItem getItem(Integer... ids)
    {
        return find(item -> { if (ids != null) for (Integer id : ids)
            if (id != null && item.getId() == id) return true; return false; });
    }
    public static GrandExchangeItem getItem(int[] ids)
    {
        return find(item -> { if (ids != null) for (int id : ids)
            if (item.getId() == id) return true; return false; });
    }
    public static GrandExchangeItem getItem(String... names)
    {
        return find(item -> { if (names != null) for (String name : names)
            if (name != null && item.getName().equalsIgnoreCase(name)) return true; return false; });
    }
    public static GrandExchangeItem getItem(Filter<Item> filter)
    {
        return filter == null ? null : find(value -> {
            Item item = value.getItem(); return item != null && filter.match(item); });
    }

    private static GrandExchangeItem find(Predicate<GrandExchangeItem> predicate)
    {
        for (GrandExchangeItem item : getItems()) if (predicate.test(item)) return item;
        return null;
    }
    private static Item item(int id)
    {
        if (id <= 0) return null;
        return new Item(new net.runelite.api.Item(id, 1), -1, ContainerType.INVENTORY,
            DreamBotRebornApi.requireClient().getItemDefinition(id), null);
    }
    private static boolean visible(WidgetInfo info)
    {
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(info);
        return widget != null && !widget.isHidden();
    }
    private static boolean visibleText(String text)
    {
        String needle = text.toLowerCase(Locale.ENGLISH);
        return first(value -> value.visible && clean(value.text + " " + value.name)
            .toLowerCase(Locale.ENGLISH).contains(needle)) != null;
    }
    private static WidgetChild first(Predicate<WidgetChild> predicate)
    {
        return Widgets.getMatchingWidget(predicate);
    }
    private static WidgetChild button(String text)
    {
        String needle = text.toLowerCase(Locale.ENGLISH);
        return first(value -> value.visible && (clean(value.text + " " + value.name)
            .toLowerCase(Locale.ENGLISH).contains(needle)
            || value.actions.stream().anyMatch(action -> action.equalsIgnoreCase(text))));
    }
    private static boolean click(WidgetChild child)
    {
        if (child == null) return false;
        return child.hasAction("Select") ? child.interact("Select")
            : child.hasAction("Continue") ? child.interact("Continue")
            : child.actions.isEmpty() ? child.click() : child.interact();
    }
    private static boolean clickNth(Predicate<WidgetChild> predicate, int index)
    {
        List<WidgetChild> values = Widgets.getAll(predicate);
        return index >= 0 && index < values.size() && click(values.get(index));
    }
    private static boolean clickOfferAction(int slot, String action)
    {
        if (!isSlotEnabled(slot) || slotContainsItem(slot)) return false;
        List<WidgetChild> widgets = Widgets.getAll(value -> value.visible && value.hasAction(action));
        return slot < widgets.size() && widgets.get(slot).interact(action);
    }
    private static WidgetChild firstItemWidget(int number)
    {
        List<WidgetChild> values = Widgets.getAll(value -> value.visible && value.itemId >= 0);
        return number >= 0 && number < values.size() ? values.get(number) : null;
    }
    private static int valueNear(String label)
    {
        String needle = label.toLowerCase(Locale.ENGLISH);
        for (WidgetChild child : Widgets.getAll(value -> value.visible
            && clean(value.text).toLowerCase(Locale.ENGLISH).contains(needle)))
        {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("([0-9][0-9,]*)")
                .matcher(clean(child.text));
            if (matcher.find()) try { return Integer.parseInt(matcher.group(1).replace(",", "")); }
            catch (NumberFormatException ignored) { return 0; }
        }
        return 0;
    }
    private static String clean(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]*>", "").trim();
    }
}
