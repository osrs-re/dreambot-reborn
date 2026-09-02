package com.dreambotreborn.api.input;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import com.dreambotreborn.api.input.keyboard.KeyboardProfile;
import com.dreambotreborn.api.input.keyboard.KeyboardTypingAlgorithm;
import com.dreambotreborn.api.input.keyboard.StandardKeyboardAlgorithm;
import com.dreambotreborn.api.utilities.impl.Condition;
import net.runelite.api.Client;

/** Visible AWT keyboard input with configurable cadence and real key holds. */
public final class Keyboard
{
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(runnable ->
    {
        Thread thread = new Thread(runnable, "DreamBot Reborn Virtual Keyboard");
        thread.setDaemon(true);
        return thread;
    });
    private static final Set<Integer> PRESSED = Collections.synchronizedSet(new HashSet<>());
    private static final ThreadLocal<Integer> SYNTHETIC_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final AtomicLong GENERATION = new AtomicLong();
    private static final AtomicInteger ACTIVE_EVENTS = new AtomicInteger();
    private static volatile KeyboardTypingAlgorithm algorithm = new StandardKeyboardAlgorithm();
    private static volatile Client client;
    private static volatile Component component;

    private Keyboard()
    {
    }

    public static void initialize(Client clientValue, Component value)
    {
        client = clientValue;
        component = value;
        cancelAll();
    }

    public static KeyboardTypingAlgorithm getKeyboardTypingAlgorithm()
    {
        return algorithm;
    }

    public static void setKeyboardTypingAlgorithm(KeyboardTypingAlgorithm value)
    {
        algorithm = value == null ? new StandardKeyboardAlgorithm() : value;
    }

    public static CompletableFuture<Boolean> type(Object value)
    {
        return type(value, false);
    }

    public static CompletableFuture<Boolean> type(Object value, boolean pressEnter)
    {
        String text = String.valueOf(value == null ? "" : value);
        long generation = GENERATION.get();
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        WORKER.execute(() ->
        {
            ACTIVE_EVENTS.incrementAndGet();
            try
            {
                char previous = 0;
                for (int index = 0; index < text.length(); index++)
                {
                    if (generation != GENERATION.get())
                    {
                        result.complete(false);
                        return;
                    }
                    char intended = text.charAt(index);
                    Character mistake = algorithm.mistakeFor(intended);
                    if (mistake != null)
                    {
                        emitCharacter(mistake, algorithm.keyHoldMillis(mistake));
                        sleep(algorithm.delayBefore(previous, mistake));
                        emitKey(KeyEvent.VK_BACK_SPACE, '\b', 35);
                        sleep(algorithm.delayBefore(mistake, intended));
                    }
                    emitCharacter(intended, algorithm.keyHoldMillis(intended));
                    sleep(algorithm.delayBefore(previous, intended));
                    previous = intended;
                }
                if (pressEnter) emitKey(KeyEvent.VK_ENTER, '\n', 45);
                result.complete(true);
            }
            catch (RuntimeException ex)
            {
                result.completeExceptionally(ex);
            }
            finally
            {
                ACTIVE_EVENTS.decrementAndGet();
            }
        });
        return result;
    }

    public static CompletableFuture<Boolean> type(Object value, boolean pressEnter, Condition condition)
    {
        if (condition != null && condition.verify()) return CompletableFuture.completedFuture(true);
        return type(value, pressEnter).thenApply(typed -> typed && (condition == null || condition.verify()));
    }

    public static CompletableFuture<Boolean> typeKey(int keyCode)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        WORKER.execute(() ->
        {
            ACTIVE_EVENTS.incrementAndGet();
            try
            {
                emitKey(keyCode, KeyEvent.CHAR_UNDEFINED, 42);
                result.complete(true);
            }
            catch (RuntimeException ex)
            {
                result.completeExceptionally(ex);
            }
            finally
            {
                ACTIVE_EVENTS.decrementAndGet();
            }
        });
        return result;
    }

    public static CompletableFuture<Boolean> pressEsc() { return typeKey(KeyEvent.VK_ESCAPE); }
    public static CompletableFuture<Boolean> pressShift() { return pressKey(KeyEvent.VK_SHIFT); }
    public static CompletableFuture<Boolean> releaseShift() { return releaseKey(KeyEvent.VK_SHIFT); }

    public static CompletableFuture<Boolean> holdKey(int keyCode, Condition condition)
    {
        return holdKey(keyCode, condition, 10_000L);
    }

    public static CompletableFuture<Boolean> holdKey(int keyCode, Condition condition, long timeout)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        long generation = GENERATION.get();
        WORKER.execute(() ->
        {
            pressKeyNow(keyCode);
            long deadline = System.nanoTime() + Math.max(0L, timeout) * 1_000_000L;
            while (generation == GENERATION.get() && System.nanoTime() < deadline
                && condition != null && !condition.verify()) sleep(20);
            releaseKeyNow(keyCode);
            result.complete(generation == GENERATION.get()
                && (condition == null || condition.verify()));
        });
        return result;
    }

    public static CompletableFuture<Boolean> holdShift(Condition condition) { return holdKey(KeyEvent.VK_SHIFT, condition); }
    public static CompletableFuture<Boolean> holdControl(Condition condition) { return holdKey(KeyEvent.VK_CONTROL, condition); }
    public static CompletableFuture<Boolean> holdSpace(Condition condition) { return holdKey(KeyEvent.VK_SPACE, condition); }

    public static CompletableFuture<Boolean> pressKey(int keyCode)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        EventQueue.invokeLater(() -> { pressKeyNow(keyCode); result.complete(true); });
        return result;
    }

    public static CompletableFuture<Boolean> releaseKey(int keyCode)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        EventQueue.invokeLater(() -> { releaseKeyNow(keyCode); result.complete(true); });
        return result;
    }

    public static boolean isPressed(int keyCode) { return PRESSED.contains(keyCode); }
    public static boolean isTyping() { return ACTIVE_EVENTS.get() > 0; }
    public static boolean isHeld(int keyCode) { return isPressed(keyCode); }
    public static int getCurrentModifiers()
    {
        int modifiers = 0;
        if (isPressed(KeyEvent.VK_SHIFT)) modifiers |= InputEvent.SHIFT_DOWN_MASK;
        if (isPressed(KeyEvent.VK_CONTROL)) modifiers |= InputEvent.CTRL_DOWN_MASK;
        if (isPressed(KeyEvent.VK_ALT)) modifiers |= InputEvent.ALT_DOWN_MASK;
        if (isPressed(KeyEvent.VK_META)) modifiers |= InputEvent.META_DOWN_MASK;
        return modifiers;
    }

    public static double getWordsPerMinute() { return KeyboardProfile.getWordsPerMinute(); }
    public static void setWordsPerMinute(double value) { KeyboardProfile.setWordsPerMinute(value); }
    public static boolean isSyntheticInputEvent() { return SYNTHETIC_DEPTH.get() > 0; }
    public static void loseFocus() { dispatchFocus(FocusEvent.FOCUS_LOST); }
    public static void gainFocus() { dispatchFocus(FocusEvent.FOCUS_GAINED); }

    public static void cancelAll()
    {
        GENERATION.incrementAndGet();
        for (Integer keyCode : new HashSet<>(PRESSED)) releaseKeyNow(keyCode);
        PRESSED.clear();
    }

    private static void emitCharacter(char value, int holdMillis)
    {
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(value);
        emitKey(keyCode == KeyEvent.VK_UNDEFINED ? 0 : keyCode, value, holdMillis);
    }

    private static void emitKey(int keyCode, char value, int holdMillis)
    {
        pressKeyNow(keyCode);
        if (value != KeyEvent.CHAR_UNDEFINED)
        {
            dispatch(new KeyEvent(requireComponent(), KeyEvent.KEY_TYPED,
                System.currentTimeMillis(), getCurrentModifiers(), KeyEvent.VK_UNDEFINED, value));
        }
        sleep(Math.max(1, holdMillis));
        releaseKeyNow(keyCode);
    }

    private static void pressKeyNow(int keyCode)
    {
        PRESSED.add(keyCode);
        dispatch(new KeyEvent(requireComponent(), KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(), getCurrentModifiers(), keyCode, KeyEvent.CHAR_UNDEFINED));
    }

    private static void releaseKeyNow(int keyCode)
    {
        Component target = component;
        if (target == null) return;
        dispatch(new KeyEvent(target, KeyEvent.KEY_RELEASED,
            System.currentTimeMillis(), getCurrentModifiers(), keyCode, KeyEvent.CHAR_UNDEFINED));
        PRESSED.remove(keyCode);
    }

    private static void dispatch(KeyEvent event)
    {
        Runnable action = () ->
        {
            SYNTHETIC_DEPTH.set(SYNTHETIC_DEPTH.get() + 1);
            try { requireComponent().dispatchEvent(event); }
            finally
            {
                int depth = SYNTHETIC_DEPTH.get() - 1;
                if (depth <= 0) SYNTHETIC_DEPTH.remove(); else SYNTHETIC_DEPTH.set(depth);
            }
        };
        if (EventQueue.isDispatchThread()) action.run();
        else
        {
            try { EventQueue.invokeAndWait(action); }
            catch (Exception ex) { throw new IllegalStateException("Unable to dispatch keyboard input", ex); }
        }
    }

    private static void dispatchFocus(int id)
    {
        EventQueue.invokeLater(() -> requireComponent().dispatchEvent(
            new FocusEvent(requireComponent(), id)));
    }

    private static Component requireComponent()
    {
        Client currentClient = client;
        Component target = currentClient != null && currentClient.getCanvas() != null
            ? currentClient.getCanvas() : component;
        if (target == null) throw new IllegalStateException("Keyboard has not been initialized");
        return target;
    }

    private static void sleep(long milliseconds)
    {
        try { Thread.sleep(Math.max(0L, milliseconds)); }
        catch (InterruptedException ex) { Thread.currentThread().interrupt(); }
    }
}
