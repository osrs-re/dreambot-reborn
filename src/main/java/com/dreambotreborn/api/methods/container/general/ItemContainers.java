package com.dreambotreborn.api.methods.container.general;

import com.dreambotreborn.api.DreamBotRebornApi;

public final class ItemContainers
{
    private ItemContainers() { }
    public static ItemContainer getContainer(int id)
    {
        net.runelite.api.ItemContainer container = DreamBotRebornApi.requireClient().getItemContainer(id);
        return container == null ? null : new ItemContainer(container, id);
    }
    public static ItemContainer getContainer(ItemContainerId id)
    {
        return id == null ? null : getContainer(id.getId());
    }
}
