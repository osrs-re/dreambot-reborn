package com.dreambotreborn.api.wrappers.items;

import java.awt.Component;
import java.awt.Shape;
import net.runelite.api.MenuAction;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.WorldView;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.wrappers.interactive.Entity;

/** Immutable item-on-the-ground snapshot. */
public final class GroundItem extends Entity
{
    public final int amount;

    private final TileItem item;
    private final Tile tile;

    public GroundItem(TileItem item, Tile tile, String name)
    {
        // The standard ground-item operation occupies the third action slot.
        super(item.getId(), name, new String[] {null, null, "Take", null, null},
            tile.getWorldLocation(), tile.getLocalLocation());
        this.item = item;
        this.tile = tile;
        this.amount = item.getQuantity();
    }

    public int getAmount()
    {
        return amount;
    }

    public int getQuantity()
    {
        return amount;
    }

    @Override
    public Shape clickShape(Component component)
    {
        return localLocation == null ? null
            : net.runelite.api.Perspective.getCanvasTilePoly(
                DreamBotRebornApi.requireClient(), localLocation);
    }

    @Override
    public InteractionSpec interactionAt(int index)
    {
        if (localLocation == null || index < 0 || index >= 5)
        {
            return null;
        }
        MenuAction[] actions =
        {
            MenuAction.GROUND_ITEM_FIRST_OPTION,
            MenuAction.GROUND_ITEM_SECOND_OPTION,
            MenuAction.GROUND_ITEM_THIRD_OPTION,
            MenuAction.GROUND_ITEM_FOURTH_OPTION,
            MenuAction.GROUND_ITEM_FIFTH_OPTION
        };
        return new InteractionSpec(
            localLocation.getSceneX(),
            localLocation.getSceneY(),
            DreamBotRebornApi.requireClient().isWidgetSelected()
                ? MenuAction.WIDGET_TARGET_ON_GROUND_ITEM : actions[index],
            id,
            actionAt(index),
            name,
            WorldView.TOPLEVEL);
    }

    public TileItem unwrap()
    {
        return item;
    }

    @Override
    public String toString()
    {
        return "GroundItem{" + "id=" + id + ", name='" + name + '\'' +
            ", amount=" + amount + ", worldLocation=" + worldLocation + '}';
    }
}
