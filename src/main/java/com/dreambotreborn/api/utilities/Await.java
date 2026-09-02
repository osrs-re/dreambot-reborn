package com.dreambotreborn.api.utilities;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.dreambotreborn.api.internal.ClientThread;

/** Bridges DreamBot Reborn's non-blocking input engine to DreamBot-style boolean APIs. */
public final class Await
{
    private static final long DEFAULT_TIMEOUT_SECONDS = 30L;

    private Await()
    {
    }

    public static boolean success(CompletableFuture<Boolean> future)
    {
        return Boolean.TRUE.equals(result(future, false));
    }

    public static <T> T result(CompletableFuture<T> future, T fallback)
    {
        if (future == null)
        {
            return fallback;
        }
        // The client thread must keep pumping input. An incomplete future here
        // means the command was accepted and will finish on following ticks.
        if (ClientThread.isClientThread() && !future.isDone())
        {
            return fallback instanceof Boolean ? castBoolean(true) : fallback;
        }
        try
        {
            return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            return fallback;
        }
        catch (ExecutionException | TimeoutException ex)
        {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T castBoolean(boolean value)
    {
        return (T) Boolean.valueOf(value);
    }
}
