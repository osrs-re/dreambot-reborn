package com.dreambotreborn.api.methods.fairyring;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.interactive.GameObject;
import com.dreambotreborn.api.wrappers.widgets.Widget;
import net.runelite.api.Varbits;
import net.runelite.api.widgets.WidgetInfo;

/** Fairy-ring interface helper backed by current varbits and visible widgets. */
public final class FairyRings
{
    private static final String[][] DIALS = {{"A", "D", "C", "B"},
        {"I", "L", "K", "J"}, {"P", "S", "R", "Q"}};
    private FairyRings() { }

    public static Rectangle getQuickTravelRect()
    {
        Widget match = quickTravelWidget(getCurrentCode());
        return match == null ? null : match.getBounds();
    }

    public static boolean travelInterfaceOpen()
    {
        net.runelite.api.widgets.Widget widget =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.FAIRY_RING);
        return widget != null && !widget.isHidden();
    }

    public static boolean travel(FairyLocation location)
    { return location != null && travel(location.getCode()); }

    public static boolean travel(String[] code)
    {
        if (!valid(code) || !openTravelInterface()) return false;
        if (quickTravelContains(code) && quickTravel(code)) return true;
        if (!enterTravelCode(code)) return false;
        net.runelite.api.widgets.Widget button =
            DreamBotRebornApi.requireClient().getWidget(WidgetInfo.FAIRY_RING_TELEPORT_BUTTON);
        return button != null && new Widget(button).click();
    }

    public static boolean openTravelInterface()
    {
        if (travelInterfaceOpen()) return true;
        GameObject ring = GameObjects.closest(object -> object.hasAction("Configure")
            || object.hasAction("Fairy ring"));
        return ring != null && (ring.hasAction("Configure") ? ring.interact("Configure")
            : ring.interact("Fairy ring"));
    }

    public static boolean quickTravel(FairyLocation location)
    { return location != null && quickTravel(location.getCode()); }

    public static boolean quickTravel(String[] code)
    {
        Widget widget = quickTravelWidget(code);
        return widget != null && (widget.hasAction("Travel") ? widget.interact("Travel") : widget.click());
    }

    public static boolean scrollToQuickTravel(String[] code)
    {
        // RuneLite exposes the favorite/list entry even when clipped; interaction scrolls it into view.
        return quickTravelWidget(code) != null;
    }

    public static boolean quickTravelContains(FairyLocation location)
    { return location != null && quickTravelContains(location.getCode()); }

    public static boolean quickTravelContains(String[] code) { return quickTravelWidget(code) != null; }

    public static boolean codeEquals(String[] code)
    {
        if (!valid(code)) return false;
        String[] current = getCurrentCode();
        for (int index = 0; index < 3; index++)
            if (!current[index].equalsIgnoreCase(code[index])) return false;
        return true;
    }

    public static boolean enterTravelCode(FairyLocation location)
    { return location != null && enterTravelCode(location.getCode()); }

    public static boolean enterTravelCode(String[] code)
    {
        if (!valid(code) || !travelInterfaceOpen()) return false;
        for (int slot = 0; slot < 3; slot++)
            if (!enterCode(slot, code[slot])) return false;
        return true;
    }

    public static boolean enterCode(int slot, String letter)
    {
        if (slot < 0 || slot >= 3 || letter == null) return false;
        String wanted = letter.toUpperCase(java.util.Locale.ENGLISH);
        int wantedIndex = indexOf(DIALS[slot], wanted);
        int currentIndex = indexOf(DIALS[slot], getCode(slot));
        if (wantedIndex < 0 || currentIndex < 0) return false;
        int rotations = (wantedIndex - currentIndex + DIALS[slot].length) % DIALS[slot].length;
        for (int count = 0; count < rotations; count++)
            if (!rotateSlotClockwise(slot)) return false;
        return true;
    }

    public static boolean rotateSlotClockwise(int slot)
    {
        List<Widget> buttons = new ArrayList<>(Widgets.find(widget -> widget.isVisible()
            && widget.hasAction("Rotate clockwise")).all());
        buttons.sort(Comparator.comparingInt(widget -> widget.bounds == null
            ? Integer.MAX_VALUE : widget.bounds.x));
        return slot >= 0 && slot < buttons.size() && buttons.get(slot).interact("Rotate clockwise");
    }

    public static String[] getCurrentCode()
    { return new String[] {getCode(0), getCode(1), getCode(2)}; }

    public static String getCode(int slot)
    {
        int value;
        if (slot == 0) value = DreamBotRebornApi.requireClient().getVarbitValue(Varbits.FAIRY_RING_DIAL_ADCB);
        else if (slot == 1) value = DreamBotRebornApi.requireClient().getVarbitValue(Varbits.FAIRY_RIGH_DIAL_ILJK);
        else if (slot == 2) value = DreamBotRebornApi.requireClient().getVarbitValue(Varbits.FAIRY_RING_DIAL_PSRQ);
        else return "";
        return value < 0 || value >= DIALS[slot].length ? "" : DIALS[slot][value];
    }

    private static Widget quickTravelWidget(String[] code)
    {
        if (!valid(code)) return null;
        String wanted = (code[0] + code[1] + code[2]).toUpperCase(java.util.Locale.ENGLISH);
        return Widgets.find(widget -> widget.isVisible() && normalized(widget.getText()).contains(wanted)
            && (widget.hasAction("Travel") || widget.getText().length() > 0)).first();
    }

    private static String normalized(String value)
    { return value == null ? "" : value.replaceAll("[^A-Za-z]", "").toUpperCase(java.util.Locale.ENGLISH); }
    private static boolean valid(String[] code)
    {
        if (code == null || code.length != 3) return false;
        for (int slot = 0; slot < 3; slot++) if (indexOf(DIALS[slot], code[slot]) < 0) return false;
        return true;
    }
    private static int indexOf(String[] values, String expected)
    {
        if (expected != null) for (int index = 0; index < values.length; index++)
            if (values[index].equalsIgnoreCase(expected)) return index;
        return -1;
    }
}
