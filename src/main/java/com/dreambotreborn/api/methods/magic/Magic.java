package com.dreambotreborn.api.methods.magic;

import net.runelite.api.Varbits;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.methods.skills.Skills;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.items.Item;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

/** Spell selection/casting through the visible spellbook widgets. */
public final class Magic
{
    private static volatile Spell autocastSpell;
    private static volatile boolean defensiveAutocast;
    private static volatile boolean filteringEnabled;
    private Magic() { }

    public static boolean interact(Spell spell, String action)
    {
        if (spell == null || !Tabs.open(Tab.MAGIC)) return false;
        WidgetChild widget = spell.getWidget();
        if (widget == null || !widget.isVisible()) return false;
        return action == null ? widget.interact() : widget.interact(action);
    }
    public static boolean castSpell(Spell spell) { return castSpell(spell, "Cast"); }
    public static boolean castSpell(Spell spell, String action)
    {
        return canCast(spell) && interact(spell, action);
    }
    public static boolean castSpellOn(Spell spell, Entity entity)
    {
        return entity != null && castSpell(spell) && entity.interact();
    }
    public static boolean castSpellOn(Spell spell, Item item)
    {
        return item != null && castSpell(spell) && item.interact();
    }
    public static boolean isSpellSelected()
    {
        return DreamBotRebornApi.requireClient().isWidgetSelected()
            && DreamBotRebornApi.requireClient().getSelectedWidget() != null
            && (DreamBotRebornApi.requireClient().getSelectedWidget().getId() >>> 16) == 218;
    }
    public static String getSelectedSpellName()
    {
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getSelectedWidget();
        if (!isSpellSelected() || widget == null) return null;
        String name = widget.getName();
        return name == null ? null : name.replaceAll("<[^>]*>", "");
    }
    public static boolean canCast(Spell spell) { return canCast(spell, true); }
    public static boolean canCast(Spell spell, boolean includeEquipment)
    {
        if (spell == null || Skills.getRealLevel(Skill.MAGIC) < spell.getLevel()) return false;
        for (com.dreambotreborn.api.methods.magic.cost.Rune rune : spell.getCost())
            if (Inventory.count(rune.getName()) < rune.getAmount()) return false;
        return true;
    }
    public static boolean deselect()
    {
        return !isSpellSelected() || Inventory.deselect();
    }
    public static Spell getAutocastSpell() { return autocastSpell; }
    public static boolean isAutocasting() { return autocastSpell != null; }
    public static boolean isAutocastDefensive() { return defensiveAutocast && isAutocasting(); }
    public static boolean setDefensiveAutocastSpell(Spell spell)
    {
        defensiveAutocast = true;
        return setAutocastSpell(spell);
    }
    public static boolean setAutocastSpell(Spell spell)
    {
        if (spell == null || !canCast(spell)) return false;
        autocastSpell = spell;
        return interact(spell, "Autocast") || interact(spell, "Cast");
    }
    public static boolean isSpellFilteringEnabled() { return filteringEnabled; }
    public static boolean setSpellFilteringEnabled(boolean enabled)
    {
        if (filteringEnabled == enabled) return true;
        com.dreambotreborn.api.wrappers.widgets.Widget widget = Widgets.find(value ->
            value.groupId == 218 && value.visible
                && (value.text.toLowerCase().contains("filter")
                    || value.actions.stream().anyMatch(action ->
                        action.toLowerCase().contains("filter")))).first();
        if (widget == null || !(widget.actions.isEmpty() ? widget.click() : widget.interact()))
            return false;
        filteringEnabled = enabled;
        return true;
    }
    public static Spellbook getSpellbook()
    {
        int value = DreamBotRebornApi.requireClient().getVarbitValue(Varbits.SPELLBOOK);
        Spellbook[] books = Spellbook.values();
        return value >= 0 && value < books.length ? books[value] : Spellbook.NORMAL;
    }
}
