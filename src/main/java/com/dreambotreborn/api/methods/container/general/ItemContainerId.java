package com.dreambotreborn.api.methods.container.general;

import net.runelite.api.InventoryID;

public enum ItemContainerId
{
    TRADE(InventoryID.TRADE.getId()), TRADE_OTHER(InventoryID.TRADEOTHER.getId()),
    INVENTORY(InventoryID.INVENTORY.getId()), EQUIPMENT(InventoryID.EQUIPMENT.getId()),
    BANK(InventoryID.BANK.getId()), GE_SLOT_1(518), GE_SLOT_2(519), GE_SLOT_3(520),
    GE_SLOT_4(521), GE_SLOT_5(522), GE_SLOT_6(523), GE_SLOT_7(524), GE_SLOT_8(525),
    LOOTING_BAG(516), BARROWS_CHEST(141),
    CHAMBERS_OF_XERIC_CHEST(InventoryID.CHAMBERS_OF_XERIC_CHEST.getId()),
    THEATRE_OF_BLOOD_CHEST(InventoryID.THEATRE_OF_BLOOD_CHEST.getId()),
    WILDERNESS_LOOT_CHEST(InventoryID.WILDERNESS_LOOT_CHEST.getId()),
    ZULRAH_DEATH_COLLECT(545), TOA_REWARD_CHEST(InventoryID.TOA_REWARD_CHEST.getId());

    private final int id;
    ItemContainerId(int id) { this.id = id; }
    public int getId() { return id; }
}
