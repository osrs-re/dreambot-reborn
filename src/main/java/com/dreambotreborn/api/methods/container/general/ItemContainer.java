package com.dreambotreborn.api.methods.container.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.wrappers.items.Item;

/** Read-only snapshot wrapper for any client item-container id. */
public class ItemContainer
{
    private final net.runelite.api.ItemContainer container;
    private final int containerId;
    public ItemContainer(net.runelite.api.ItemContainer container, int containerId)
    {
        this.container = container;
        this.containerId = containerId;
    }
    public List<Item> getItems()
    {
        if (container == null || container.getItems() == null) return Collections.emptyList();
        List<Item> result = new ArrayList<>();
        net.runelite.api.Item[] values = container.getItems();
        ContainerType type = type(containerId);
        for (int slot = 0; slot < values.length; slot++)
        {
            net.runelite.api.Item value = values[slot];
            if (value != null && value.getId() >= 0 && value.getQuantity() > 0)
                result.add(new Item(value, slot, type,
                    DreamBotRebornApi.requireClient().getItemDefinition(value.getId()), null));
        }
        return Collections.unmodifiableList(result);
    }
    public int getContainerId() { return containerId; }
    private static ContainerType type(int id)
    {
        if (id == net.runelite.api.InventoryID.BANK.getId()) return ContainerType.BANK;
        if (id == net.runelite.api.InventoryID.EQUIPMENT.getId()) return ContainerType.EQUIPMENT;
        if (id == net.runelite.api.InventoryID.TRADE.getId()) return ContainerType.TRADE;
        if (id == net.runelite.api.InventoryID.TRADEOTHER.getId()) return ContainerType.TRADE_OTHER;
        return ContainerType.INVENTORY;
    }
}
