package com.dreambotreborn.api.methods.container.impl.equipment;

/** Equipment container slots, matching the DreamBot naming. */
public enum EquipmentSlot
{
    HAT(0),
    CAPE(1),
    AMULET(2),
    WEAPON(3),
    CHEST(4),
    SHIELD(5),
    LEGS(7),
    HANDS(9),
    FEET(10),
    RING(12),
    ARROWS(13);

    private final int slot;

    EquipmentSlot(int slot)
    {
        this.slot = slot;
    }

    public int getSlot()
    {
        return slot;
    }

    public static EquipmentSlot forSlotId(int slot)
    {
        for (EquipmentSlot value : values())
        {
            if (value.slot == slot)
            {
                return value;
            }
        }
        return null;
    }
}
