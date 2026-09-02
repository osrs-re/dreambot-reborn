package com.dreambotreborn.api.internal;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/** Serializes client reads and mutations onto the injected game's callback thread. */
public final class ClientThread
{
    private static final ConcurrentLinkedQueue<Task<?>> TASKS = new ConcurrentLinkedQueue<>();
    private static volatile Thread clientThread;

    private ClientThread()
    {
    }

    public static boolean isClientThread()
    {
        return Thread.currentThread() == clientThread;
    }

    public static void assertClientThread()
    {
        if (!isClientThread())
        {
            throw new IllegalStateException("This operation must run on the game client thread");
        }
    }

    public static CompletableFuture<Void> invokeLater(Runnable runnable)
    {
        Objects.requireNonNull(runnable, "runnable");
        return invoke(() ->
        {
            runnable.run();
            return null;
        });
    }

    public static <T> CompletableFuture<T> invoke(Supplier<T> supplier)
    {
        Objects.requireNonNull(supplier, "supplier");
        if (isClientThread())
        {
            try
            {
                return CompletableFuture.completedFuture(supplier.get());
            }
            catch (Throwable error)
            {
                CompletableFuture<T> failed = new CompletableFuture<>();
                failed.completeExceptionally(error);
                return failed;
            }
        }
        Task<T> task = new Task<>(supplier);
        TASKS.add(task);
        return task.result;
    }

    /** Called before every API/input tick by the injected client callback. */
    public static void pump()
    {
        clientThread = Thread.currentThread();
        int handled = 0;
        Task<?> task;
        while (handled++ < 1024 && (task = TASKS.poll()) != null)
        {
            task.run();
        }
    }

    public static void shutdown()
    {
        Task<?> task;
        while ((task = TASKS.poll()) != null)
        {
            task.result.completeExceptionally(new IllegalStateException("Client stopped"));
        }
        clientThread = null;
    }

    private static final class Task<T>
    {
        private final Supplier<T> supplier;
        private final CompletableFuture<T> result = new CompletableFuture<>();

        private Task(Supplier<T> supplier)
        {
            this.supplier = supplier;
        }

        private void run()
        {
            try { result.complete(supplier.get()); }
            catch (Throwable error) { result.completeExceptionally(error); }
        }
    }
}
