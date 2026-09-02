package com.dreambotreborn.api.methods.walking.web.node;

import java.util.Locale;

public enum WebNodeType
{
    BASIC_NODE, ENTRANCE_NODE, TOLL_NODE, BANK_NODE, AGILITY_NODE,
    CHARTER_NODE, SPIRIT_TREE_NODE, FAIRY_RING_NODE, TELEPORT_NODE,
    SHIP_NODE, MAGIC_CARPET_NODE, MOUNTAIN_GUIDE_NODE, QUETZAL_NODE,
    LOVAKENGJ_MINECART_NODE;
    public int getId() { return ordinal(); }
    @Override public String toString()
    {
        return name().replace('_', ' ').toLowerCase(Locale.ENGLISH);
    }
}
