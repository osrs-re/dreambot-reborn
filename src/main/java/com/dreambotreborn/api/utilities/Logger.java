package com.dreambotreborn.api.utilities;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Bounded script/client log with UI subscriptions. */
public final class Logger
{
    public enum LogType { DEBUG, INFO, WARN, ERROR }

    public static final class Entry
    {
        public final Instant timestamp;
        public final LogType type;
        public final String message;

        private Entry(LogType type, String message)
        {
            this.timestamp = Instant.now();
            this.type = type;
            this.message = message;
        }
    }

    private static final Object LOCK = new Object();
    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();
    private static final CopyOnWriteArrayList<Consumer<Entry>> LISTENERS = new CopyOnWriteArrayList<>();
    private static final int CAPACITY = 1000;

    private Logger()
    {
    }

    public static void debug(Object value) { log(LogType.DEBUG, value); }
    public static void info(Object value) { log(LogType.INFO, value); }
    public static void warn(Object value) { log(LogType.WARN, value); }
    public static void error(Object value) { log(LogType.ERROR, value); }
    public static void log(Object value) { log(LogType.INFO, value); }

    public static void log(LogType type, Object value)
    {
        Entry entry = new Entry(type == null ? LogType.INFO : type, String.valueOf(value));
        synchronized (LOCK)
        {
            ENTRIES.addLast(entry);
            while (ENTRIES.size() > CAPACITY) ENTRIES.removeFirst();
        }
        if (entry.type == LogType.ERROR || entry.type == LogType.WARN)
            System.err.println('[' + entry.type.name() + "] " + entry.message);
        else System.out.println('[' + entry.type.name() + "] " + entry.message);
        for (Consumer<Entry> listener : LISTENERS) listener.accept(entry);
    }

    public static List<Entry> entries()
    {
        synchronized (LOCK) { return new ArrayList<>(ENTRIES); }
    }

    public static void clear()
    {
        synchronized (LOCK) { ENTRIES.clear(); }
    }

    public static void subscribe(Consumer<Entry> listener) { if (listener != null) LISTENERS.addIfAbsent(listener); }
    public static void unsubscribe(Consumer<Entry> listener) { LISTENERS.remove(listener); }
}
