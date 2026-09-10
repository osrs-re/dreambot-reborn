package com.dreambotreborn.api.methods.grandexchange;

import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.wrappers.items.Item;

/** Guide-price access from the locally loaded item definitions. */
public final class LivePrices
{
    private LivePrices() { }
    public static int get(int id)
    {
        net.runelite.api.ItemComposition definition = id < 0 ? null
            : DreamBotRebornApi.requireClient().getItemDefinition(id);
        return definition == null ? 0 : definition.getPrice();
    }
    public static int get(String name) { return get(findId(name)); }
    public static int get(Item item) { return item == null ? 0 : get(item.getId()); }
    public static int getHigh(int id) { return get(id); }
    public static int getHigh(String name) { return get(name); }
    public static int getHigh(Item item) { return get(item); }
    public static int getLow(int id) { return get(id); }
    public static int getLow(String name) { return get(name); }
    public static int getLow(Item item) { return get(item); }

    private static int findId(String name)
    {
        if (name == null || name.trim().isEmpty()) return -1;
        net.runelite.api.Client client = DreamBotRebornApi.requireClient();
        for (int id = 0; id < client.getItemCount(); id++)
        {
            net.runelite.api.ItemComposition definition = client.getItemDefinition(id);
            if (definition != null && name.equalsIgnoreCase(definition.getName())) return id;
        }
        return -1;
    }
}
