package com.dreambotreborn.api.methods.input;

import java.awt.event.KeyEvent;
import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.utilities.impl.Condition;

/** DreamBot package-compatible facade for DreamBot Reborn's virtual keyboard. */
public final class Keyboard
{
    private static volatile boolean typeEventOpen = true;

    private Keyboard() { }

    public static boolean closeInterfaceWithESC()
    {
        com.dreambotreborn.api.input.Keyboard.pressEsc();
        return true;
    }

    public static void type(Object value) { com.dreambotreborn.api.input.Keyboard.type(value); }
    public static void type(Object value, boolean enter)
    {
        com.dreambotreborn.api.input.Keyboard.type(value, enter);
    }
    public static void type(Object value, boolean enter, Condition condition)
    {
        com.dreambotreborn.api.input.Keyboard.type(value, enter, condition);
    }
    public static void type(Object value, boolean enter, boolean ignored)
    {
        com.dreambotreborn.api.input.Keyboard.type(value, enter);
    }
    public static CompletableFuture<Boolean> typeAsync(Object value, boolean enter)
    {
        return com.dreambotreborn.api.input.Keyboard.type(value, enter);
    }
    public static boolean isTyping() { return com.dreambotreborn.api.input.Keyboard.isTyping(); }
    public static void typeSpecialKey(int keyCode)
    {
        com.dreambotreborn.api.input.Keyboard.typeKey(keyCode);
    }
    public static void pressEsc() { com.dreambotreborn.api.input.Keyboard.pressEsc(); }
    public static void setTypeEventOpen(boolean value) { typeEventOpen = value; }
    public static boolean isTypeEventOpen() { return typeEventOpen; }
    public static void holdShift(Condition condition)
    {
        com.dreambotreborn.api.input.Keyboard.holdShift(condition);
    }
    public static void holdControl(Condition condition)
    {
        com.dreambotreborn.api.input.Keyboard.holdControl(condition);
    }
    public static void holdControl(Condition condition, long timeout)
    {
        com.dreambotreborn.api.input.Keyboard.holdKey(KeyEvent.VK_CONTROL, condition, timeout);
    }
    public static void holdShift(Condition condition, long timeout)
    {
        com.dreambotreborn.api.input.Keyboard.holdKey(KeyEvent.VK_SHIFT, condition, timeout);
    }
    public static void holdKey(char key, Condition condition, long timeout)
    {
        int code = KeyEvent.getExtendedKeyCodeForChar(key);
        com.dreambotreborn.api.input.Keyboard.holdKey(code, condition, timeout);
    }
    public static void holdSpace(Condition condition, long timeout)
    {
        com.dreambotreborn.api.input.Keyboard.holdKey(KeyEvent.VK_SPACE, condition, timeout);
    }
    public static void pressShift() { com.dreambotreborn.api.input.Keyboard.pressShift(); }
    public static void releaseShift() { com.dreambotreborn.api.input.Keyboard.releaseShift(); }
    public static boolean isHoldingShift()
    {
        return com.dreambotreborn.api.input.Keyboard.isPressed(KeyEvent.VK_SHIFT);
    }
    public static void setHoldingShift(boolean holding)
    {
        if (holding) pressShift(); else releaseShift();
    }
    public static double getWordsPerMinute()
    {
        return com.dreambotreborn.api.input.Keyboard.getWordsPerMinute();
    }
    public static void setWordsPerMinute(double value)
    {
        com.dreambotreborn.api.input.Keyboard.setWordsPerMinute(value);
    }
    public static void loseFocus() { com.dreambotreborn.api.input.Keyboard.loseFocus(); }
    public static void gainFocus() { com.dreambotreborn.api.input.Keyboard.gainFocus(); }
}
