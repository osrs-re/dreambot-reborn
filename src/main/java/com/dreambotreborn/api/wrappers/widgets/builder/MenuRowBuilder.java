package com.dreambotreborn.api.wrappers.widgets.builder;

import java.util.regex.Pattern;
import com.dreambotreborn.api.internal.MouseTarget;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.wrappers.items.GroundItem;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;
import com.dreambotreborn.api.wrappers.widgets.Widget;

/** Builds injectable menu rows from DreamBot Reborn wrapper objects. */
public final class MenuRowBuilder
{
    private MenuRowBuilder() { }

    public static MenuRow buildFromObject(GameObject object, String action)
    {
        return buildFromEntity(object, action);
    }
    public static MenuRow buildFromNpc(NPC npc, String action) { return buildFromEntity(npc, action); }
    public static MenuRow buildFromPlayer(Player player, String action)
    {
        return buildFromEntity(player, action);
    }
    public static MenuRow buildFromGroundItem(GroundItem item, String action)
    {
        return buildFromEntity(item, action);
    }
    public static MenuRow buildFromEntity(Entity entity, String action)
    {
        return entity == null ? null : fromTarget(entity, action);
    }
    public static MenuRow buildForInventory(int slot, String action)
    {
        Item item = Inventory.getItemInSlot(slot);
        return item == null ? null : fromTarget(item, action);
    }
    public static MenuRow buildForBank(int slot, String action)
    {
        Item item = com.dreambotreborn.api.methods.container.impl.bank.Bank.getItemInSlot(slot);
        return item == null ? null : fromTarget(item, action);
    }
    public static MenuRow buildForWidget(Widget widget, String action, String target)
    {
        MenuRow row = widget == null ? null : fromTarget(widget, action);
        if (row != null && target != null) row.setObject(target);
        return row;
    }
    public static MenuRow buildForWorld(int world, Widget widget)
    {
        MenuRow row = buildForWidget(widget, "Switch", "World " + world);
        if (row != null) row.setId(world);
        return row;
    }
    public static String removeFormat(String value, Pattern... patterns)
    {
        String result = value == null ? "" : value;
        if (patterns != null)
            for (Pattern pattern : patterns) if (pattern != null) result = pattern.matcher(result).replaceAll("");
        return result;
    }
    public static String removeFormatting(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]*>", "");
    }

    private static MenuRow fromTarget(MouseTarget target, String action)
    {
        int actionIndex = action == null ? target.firstActionIndex() : target.actionIndex(action);
        MouseTarget.InteractionSpec spec = target.interactionAt(actionIndex);
        if (spec == null) return null;
        return new MenuRow(spec.option, spec.target, spec.identifier, spec.param0,
            spec.param1, spec.action.getId(), -1, spec.worldViewId);
    }
}
