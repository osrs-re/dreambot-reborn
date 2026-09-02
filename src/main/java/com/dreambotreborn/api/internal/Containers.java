package com.dreambotreborn.api.internal;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.wrappers.items.Item;

public final class Containers
{
    private static volatile Map<ContainerType, List<Item>> snapshots = emptySnapshots();
    private static volatile Map<ContainerType, Boolean> loaded = emptyLoaded();

    private Containers()
    {
    }

    public static List<Item> items(ContainerType type)
    {
        List<Item> items = snapshots.get(type);
        return items == null ? Collections.emptyList() : items;
    }

    public static boolean isLoaded(ContainerType type)
    {
        return Boolean.TRUE.equals(loaded.get(type));
    }

    public static int capacity(ContainerType type)
    {
        if (type == ContainerType.INVENTORY)
        {
            return 28;
        }
        if (type == ContainerType.EQUIPMENT)
        {
            return 14;
        }
        if (type == ContainerType.SHOP)
        {
            return Math.max(40, items(type).size());
        }
        InventoryID id = inventoryId(type);
        ItemContainer container = id == null ? null : DreamBotRebornApi.requireClient().getItemContainer(id);
        return container == null || container.getItems() == null ? 0 : container.getItems().length;
    }

    public static void refresh()
    {
        Client client = DreamBotRebornApi.requireClient();
        EnumMap<ContainerType, List<Item>> next = new EnumMap<>(ContainerType.class);
        EnumMap<ContainerType, Boolean> nextLoaded = new EnumMap<>(ContainerType.class);
        for (ContainerType type : ContainerType.values())
        {
            InventoryID id = inventoryId(type);
            ItemContainer container = id == null ? null : client.getItemContainer(id);
            List<Item> values = type == ContainerType.SHOP
                ? readWidgets(client, type) : read(client, type, container);
            nextLoaded.put(type, type == ContainerType.SHOP ? rootWidget(type) != null : container != null);
            next.put(type, Collections.unmodifiableList(values));
        }
        snapshots = Collections.unmodifiableMap(next);
        loaded = Collections.unmodifiableMap(nextLoaded);
    }

    public static void clear()
    {
        snapshots = emptySnapshots();
        loaded = emptyLoaded();
    }

    public static int widgetId(ContainerType type)
    {
        Widget root = rootWidget(type);
        return root == null ? -1 : root.getId();
    }

    public static Widget rootWidget(ContainerType type)
    {
        if (type == ContainerType.INVENTORY)
        {
            Widget[] contextual =
            {
                DreamBotRebornApi.requireClient().getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER),
                DreamBotRebornApi.requireClient().getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER),
                DreamBotRebornApi.requireClient().getWidget(WidgetInfo.SHOP_INVENTORY_ITEMS_CONTAINER),
                DreamBotRebornApi.requireClient().getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER)
            };
            for (Widget widget : contextual)
                if (widget != null && !widget.isHidden()) return widget;
        }
        if (type == ContainerType.SHOP) return rootForGroup(300);
        if (type == ContainerType.TRADE) return rootForGroup(335);
        if (type == ContainerType.TRADE_OTHER) return rootForGroup(335);
        WidgetInfo info = widgetInfo(type);
        return info == null ? null : DreamBotRebornApi.requireClient().getWidget(info);
    }

    public static Widget widgetForSlot(ContainerType type, int slot)
    {
        Item item = com.dreambotreborn.api.internal.ContainerQueries.getInSlot(type, slot);
        Widget root = rootWidget(type);
        if (root == null)
        {
            return null;
        }
        if (item != null)
        {
            Widget matched = findItemWidget(root, item.id, slot);
            if (matched != null) return matched;
        }
        Widget direct = root.getChild(slot);
        return direct == null ? root : direct;
    }

    public static Rectangle approximateSlotBounds(ContainerType type, int slot)
    {
        Widget root = rootWidget(type);
        if (root == null || root.getBounds() == null)
        {
            return null;
        }
        Rectangle bounds = root.getBounds();
        if (type == ContainerType.INVENTORY)
        {
            int column = slot % 4;
            int row = slot / 4;
            return new Rectangle(bounds.x + column * bounds.width / 4,
                bounds.y + row * bounds.height / 7,
                Math.max(1, bounds.width / 4), Math.max(1, bounds.height / 7));
        }
        return new Rectangle(bounds);
    }

    private static List<Item> read(Client client, ContainerType type, ItemContainer container)
    {
        List<Item> result = new ArrayList<>();
        if (container == null || container.getItems() == null)
        {
            return result;
        }
        net.runelite.api.Item[] items = container.getItems();
        Widget root = client.getWidget(widgetInfo(type));
        for (int slot = 0; slot < items.length; slot++)
        {
            net.runelite.api.Item item = items[slot];
            if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
            {
                continue;
            }
            ItemComposition definition = client.getItemDefinition(item.getId());
            Widget widget = findItemWidget(root, item.getId(), slot);
            result.add(new Item(item, slot, type, definition, widget));
        }
        return result;
    }

    private static List<Item> readWidgets(Client client, ContainerType type)
    {
        Widget root = rootWidget(type);
        if (root == null) return Collections.emptyList();
        List<Item> result = new ArrayList<>();
        List<Widget> stack = new ArrayList<>();
        stack.add(root);
        for (int cursor = 0; cursor < stack.size(); cursor++)
        {
            Widget widget = stack.get(cursor);
            if (widget.getItemId() >= 0 && widget.getItemQuantity() > 0)
            {
                int slot = widget.getIndex() >= 0 ? widget.getIndex() : result.size();
                result.add(new Item(new net.runelite.api.Item(
                    widget.getItemId(), widget.getItemQuantity()), slot, type,
                    client.getItemDefinition(widget.getItemId()), widget));
            }
            add(stack, widget.getChildren());
            add(stack, widget.getDynamicChildren());
            add(stack, widget.getStaticChildren());
            add(stack, widget.getNestedChildren());
        }
        return result;
    }

    private static Widget findItemWidget(Widget root, int itemId, int slot)
    {
        if (root == null)
        {
            return null;
        }
        Widget fallback = null;
        List<Widget> stack = new ArrayList<>();
        stack.add(root);
        for (int cursor = 0; cursor < stack.size(); cursor++)
        {
            Widget widget = stack.get(cursor);
            if (widget.getItemId() == itemId)
            {
                if (widget.getIndex() == slot)
                {
                    return widget;
                }
                if (fallback == null)
                {
                    fallback = widget;
                }
            }
            add(stack, widget.getChildren());
            add(stack, widget.getDynamicChildren());
            add(stack, widget.getStaticChildren());
            add(stack, widget.getNestedChildren());
        }
        return fallback;
    }

    private static void add(List<Widget> stack, Widget[] children)
    {
        if (children != null)
        {
            for (Widget child : children)
            {
                if (child != null && !stack.contains(child))
                {
                    stack.add(child);
                }
            }
        }
    }

    private static InventoryID inventoryId(ContainerType type)
    {
        switch (type)
        {
            case INVENTORY:
                return InventoryID.INVENTORY;
            case EQUIPMENT:
                return InventoryID.EQUIPMENT;
            case BANK:
                return InventoryID.BANK;
            case TRADE:
                return InventoryID.TRADE;
            case TRADE_OTHER:
                return InventoryID.TRADEOTHER;
            case SHOP:
                return null;
            default:
                throw new IllegalArgumentException("Unsupported container " + type);
        }
    }

    private static WidgetInfo widgetInfo(ContainerType type)
    {
        switch (type)
        {
            case INVENTORY:
                Widget bankInventory = DreamBotRebornApi.requireClient().getWidget(
                    WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER);
                return bankInventory != null && !bankInventory.isHidden()
                    ? WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER : WidgetInfo.INVENTORY;
            case EQUIPMENT:
                return WidgetInfo.EQUIPMENT;
            case BANK:
                return WidgetInfo.BANK_ITEM_CONTAINER;
            case SHOP:
            case TRADE:
            case TRADE_OTHER:
                return null;
            default:
                return null;
        }
    }

    private static Map<ContainerType, List<Item>> emptySnapshots()
    {
        EnumMap<ContainerType, List<Item>> result = new EnumMap<>(ContainerType.class);
        for (ContainerType type : ContainerType.values())
        {
            result.put(type, Collections.emptyList());
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<ContainerType, Boolean> emptyLoaded()
    {
        EnumMap<ContainerType, Boolean> result = new EnumMap<>(ContainerType.class);
        for (ContainerType type : ContainerType.values())
        {
            result.put(type, false);
        }
        return Collections.unmodifiableMap(result);
    }

    private static Widget rootForGroup(int groupId)
    {
        Widget[] roots = DreamBotRebornApi.requireClient().getWidgetRoots();
        if (roots == null) return null;
        for (Widget root : roots)
        {
            if (root != null && (root.getId() >>> 16) == groupId) return root;
        }
        return null;
    }
}
