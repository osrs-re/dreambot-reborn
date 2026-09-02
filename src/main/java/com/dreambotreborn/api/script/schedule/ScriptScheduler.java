package com.dreambotreborn.api.script.schedule;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.api.script.schedule.conditions.StopCondition;

/** Sequential/repeating schedule runner with mandatory per-entry account preparation hooks. */
public final class ScriptScheduler
{
    private static final ScriptScheduler INSTANCE = new ScriptScheduler();
    private final AtomicBoolean running = new AtomicBoolean();
    private volatile Thread thread;
    private volatile Function<String, CompletableFuture<Boolean>> accountPreparer;
    private volatile String status = "Idle";

    private ScriptScheduler()
    {
    }

    public static boolean isRunning() { return INSTANCE.running.get(); }
    public static ScriptScheduler getScriptScheduler() { return INSTANCE; }

    public void setAccountPreparer(Function<String, CompletableFuture<Boolean>> value)
    {
        accountPreparer = value;
    }

    public boolean start(ScriptSchedule schedule)
    {
        Objects.requireNonNull(schedule, "schedule");
        if (!running.compareAndSet(false, true)) return false;
        ScriptSchedule copy = schedule.clone();
        thread = new Thread(() -> run(copy), "DreamBot Reborn Script Scheduler");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    public void reset()
    {
        running.set(false);
        Thread current = thread;
        if (current != null) current.interrupt();
        ScriptManager.getScriptManager().stopWithScheduledStop();
        status = "Idle";
    }

    public String getStatus() { return status; }

    private void run(ScriptSchedule schedule)
    {
        try
        {
            do
            {
                List<ScheduledScript> entries = schedule.getScheduledScripts();
                for (int index = 0; running.get() && index < entries.size(); index++)
                {
                    runEntry(entries.get(index), index + 1, entries.size());
                }
            }
            while (running.get() && schedule.isRepeating());
        }
        catch (InterruptedException interrupted)
        {
            Thread.currentThread().interrupt();
        }
        catch (RuntimeException error)
        {
            status = "Failed: " + error.getMessage();
        }
        finally
        {
            running.set(false);
            thread = null;
            if (!status.startsWith("Failed")) status = "Finished";
        }
    }

    private void runEntry(ScheduledScript entry, int index, int total) throws InterruptedException
    {
        status = "Preparing " + index + '/' + total + ": " + entry.getScript();
        Function<String, CompletableFuture<Boolean>> preparer = accountPreparer;
        if (preparer == null)
            throw new IllegalStateException("No account preparer is configured");
        Boolean ready = preparer.apply(entry.getAccountId()).join();
        if (!Boolean.TRUE.equals(ready))
            throw new IllegalStateException("Could not prepare the selected account");
        if (!running.get()) return;
        ScriptManager manager = ScriptManager.getScriptManager();
        manager.start(entry.getScript(), entry.getParameters());
        manager.configureLoginRecovery(() -> preparer.apply(entry.getAccountId()));
        StopCondition stop = entry.getStopCondition();
        if (stop != null) stop.reset();
        status = "Running " + index + '/' + total + ": " + entry.getScript();
        while (running.get() && manager.isRunning()
            && (stop == null || !stop.shouldStop())) Thread.sleep(250L);
        if (manager.isRunning()) manager.stopWithScheduledStop();
        while (manager.isRunning() && running.get()) Thread.sleep(50L);
    }
}
