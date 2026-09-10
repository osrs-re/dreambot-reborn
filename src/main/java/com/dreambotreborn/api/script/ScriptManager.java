package com.dreambotreborn.api.script;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.EventListener;
import com.dreambotreborn.api.input.Keyboard;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.randoms.RandomManager;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import com.dreambotreborn.api.utilities.Logger;

/** Discovers scripts and owns the lifecycle of the single active script. */
public final class ScriptManager
{
    public enum State
    {
        START,
        PAUSED,
        RUNNING,
        SKIP,
        STOP
    }

    private static final ScriptManager INSTANCE = new ScriptManager();

    private final Object lifecycleLock = new Object();
    private final RandomManager randomManager = new RandomManager();
    private final List<URLClassLoader> externalLoaders = new ArrayList<>();
    private volatile Path scriptsDirectory = Paths.get(
        System.getProperty("user.home"), ".dreambot-reborn", "scripts");
    private volatile List<ScriptDescriptor> discoveredScripts = Collections.emptyList();
    private volatile List<String> discoveryErrors = Collections.emptyList();
    private volatile AbstractScript currentScript;
    private volatile Thread scriptThread;
    private volatile State state = State.STOP;
    private volatile Throwable lastError;
    private volatile Throwable lastPaintError;
    private volatile String[] currentArguments = new String[0];

    private ScriptManager()
    {
    }

    public static ScriptManager getScriptManager()
    {
        return INSTANCE;
    }

    public void initialize(Path directory)
    {
        scriptsDirectory = Objects.requireNonNull(directory, "directory").toAbsolutePath().normalize();
        reloadScripts();
    }

    public List<ScriptDescriptor> reloadScripts()
    {
        ScriptLoader.Discovery discovery = ScriptLoader.discover(scriptsDirectory);
        synchronized (externalLoaders)
        {
            externalLoaders.addAll(discovery.classLoaders);
        }
        discoveredScripts = Collections.unmodifiableList(new ArrayList<>(discovery.scripts));
        discoveryErrors = Collections.unmodifiableList(new ArrayList<>(discovery.errors));
        return discoveredScripts;
    }

    public List<ScriptDescriptor> getDiscoveredScripts()
    {
        return discoveredScripts;
    }

    public List<String> getDiscoveryErrors()
    {
        return discoveryErrors;
    }

    public Path getScriptsDirectory()
    {
        return scriptsDirectory;
    }

    public AbstractScript start(String name, String... arguments)
    {
        Objects.requireNonNull(name, "name");
        for (ScriptDescriptor descriptor : discoveredScripts)
        {
            if (descriptor.getManifest().name().equalsIgnoreCase(name)
                || descriptor.getClassName().equals(name))
            {
                return start(descriptor.getScriptClass(), arguments);
            }
        }
        throw new IllegalArgumentException("No discovered script named " + name);
    }

    public AbstractScript start(Class<? extends AbstractScript> scriptClass, String... arguments)
    {
        Objects.requireNonNull(scriptClass, "scriptClass");
        ScriptManifest manifest = scriptClass.getAnnotation(ScriptManifest.class);
        if (manifest == null)
        {
            throw new IllegalArgumentException(scriptClass.getName() + " has no @ScriptManifest");
        }

        stopAndWaitForPrevious();
        final AbstractScript script;
        try
        {
            java.lang.reflect.Constructor<? extends AbstractScript> constructor =
                scriptClass.getDeclaredConstructor();
            if (!constructor.isAccessible())
            {
                constructor.setAccessible(true);
            }
            script = constructor.newInstance();
        }
        catch (ReflectiveOperationException | SecurityException | LinkageError ex)
        {
            throw new IllegalArgumentException("Cannot create script " + scriptClass.getName(),
                compatibilityFailure(scriptClass, ex));
        }

        currentArguments = arguments == null ? new String[0] : arguments.clone();
        script.bind(this, currentArguments);
        Thread thread = new Thread(script, "DreamBot Reborn Script — " + manifest.name());
        thread.setDaemon(true);
        synchronized (lifecycleLock)
        {
            currentScript = script;
            scriptThread = thread;
            state = State.START;
            lastError = null;
            lastPaintError = null;
        }
        thread.start();
        return script;
    }

    public AbstractScript start(Class<? extends AbstractScript> scriptClass)
    {
        return start(scriptClass, new String[0]);
    }

    /** Called only by {@link AbstractScript#run()}. */
    public void execute(AbstractScript script)
    {
        if (script != currentScript || Thread.currentThread() != scriptThread)
        {
            throw new IllegalStateException("Scripts must be started through ScriptManager");
        }

        try
        {
            script.getScriptTimer().start();
            script.onStart(script.arguments());
            synchronized (lifecycleLock)
            {
                if (currentScript == script && state != State.STOP)
                {
                    state = State.RUNNING;
                    lifecycleLock.notifyAll();
                }
            }

            while (awaitRunnable(script))
            {
                int solverDelay = randomManager.onLoop();
                int delay = solverDelay >= 0 ? solverDelay : script.onLoop();
                Thread.interrupted();
                if (delay < 0)
                {
                    synchronized (lifecycleLock)
                    {
                        if (currentScript == script)
                        {
                            state = State.STOP;
                        }
                    }
                    break;
                }
                waitAfterLoop(script, delay);
            }
        }
        catch (Throwable ex)
        {
            if (!(ex instanceof InterruptedException) || state != State.STOP)
            {
                recordFailure("Script failed", compatibilityFailure(script.getClass(), ex));
            }
        }
        finally
        {
            script.getScriptTimer().pause();
            try
            {
                script.onExit();
            }
            catch (Throwable ex)
            {
                recordFailure("Script onExit failed", compatibilityFailure(script.getClass(), ex));
            }
            synchronized (lifecycleLock)
            {
                if (currentScript == script)
                {
                    currentScript = null;
                    scriptThread = null;
                    state = State.STOP;
                    currentArguments = new String[0];
                    lifecycleLock.notifyAll();
                }
            }
        }
    }

    public boolean pause()
    {
        AbstractScript script;
        Thread thread;
        synchronized (lifecycleLock)
        {
            if (state != State.RUNNING || currentScript == null)
            {
                return false;
            }
            state = State.PAUSED;
            script = currentScript;
            thread = scriptThread;
            script.getScriptTimer().pause();
            lifecycleLock.notifyAll();
        }
        if (thread != null)
        {
            thread.interrupt();
        }
        VirtualMouse.cancelAll();
        Keyboard.cancelAll();
        invokeLifecycle("Script onPause failed", script::onPause);
        return true;
    }

    public boolean resume()
    {
        AbstractScript script;
        synchronized (lifecycleLock)
        {
            if (state != State.PAUSED || currentScript == null)
            {
                return false;
            }
            state = State.RUNNING;
            script = currentScript;
            script.getScriptTimer().resume();
            lifecycleLock.notifyAll();
        }
        invokeLifecycle("Script onResume failed", script::onResume);
        return true;
    }

    public boolean stop()
    {
        AbstractScript script;
        Thread thread;
        synchronized (lifecycleLock)
        {
            if (currentScript == null)
            {
                state = State.STOP;
                VirtualMouse.cancelAll();
                Keyboard.cancelAll();
                randomManager.reset();
                return false;
            }
            script = currentScript;
            thread = scriptThread;
            state = State.STOP;
            script.getScriptTimer().pause();
            lifecycleLock.notifyAll();
        }
        if (thread != null)
        {
            thread.interrupt();
        }
        VirtualMouse.cancelAll();
        Keyboard.cancelAll();
        randomManager.reset();
        return true;
    }

    public boolean stop(boolean logout)
    {
        return stop();
    }

    public boolean stopWithScheduledStop()
    {
        AbstractScript script = currentScript;
        if (script == null)
        {
            return false;
        }
        try
        {
            return script.onScheduledStop() && stop();
        }
        catch (Throwable ex)
        {
            recordFailure("Script onScheduledStop failed",
                compatibilityFailure(script.getClass(), ex));
            return false;
        }
    }

    public void setState(AbstractScript script, State requestedState)
    {
        Objects.requireNonNull(requestedState, "requestedState");
        if (script != currentScript)
        {
            return;
        }
        if (requestedState == State.STOP)
        {
            stop();
        }
        else if (requestedState == State.PAUSED)
        {
            pause();
        }
        else if (requestedState == State.RUNNING)
        {
            if (!resume())
            {
                synchronized (lifecycleLock)
                {
                    if (currentScript == script)
                    {
                        state = State.RUNNING;
                        lifecycleLock.notifyAll();
                    }
                }
            }
        }
        else
        {
            synchronized (lifecycleLock)
            {
                if (currentScript == script)
                {
                    state = requestedState;
                    lifecycleLock.notifyAll();
                }
            }
        }
    }

    public boolean isPaused()
    {
        return state == State.PAUSED;
    }

    public boolean isRunning()
    {
        return currentScript != null && state != State.STOP;
    }

    public State getState()
    {
        return state;
    }

    public AbstractScript getCurrentScript()
    {
        return currentScript;
    }

    public RandomManager getRandomManager()
    {
        return randomManager;
    }

    public void configureLoginRecovery(Supplier<CompletableFuture<Boolean>> loginAction)
    {
        randomManager.getLoginSolver().setLoginAction(loginAction);
    }

    public Object getLock()
    {
        return lifecycleLock;
    }

    public String[] getArgs()
    {
        return currentArguments.clone();
    }

    public void resetArgs()
    {
        currentArguments = new String[0];
    }

    public List<String> getAllScriptNames()
    {
        List<String> names = new ArrayList<>();
        for (ScriptDescriptor descriptor : discoveredScripts)
        {
            names.add(descriptor.getManifest().name());
        }
        return Collections.unmodifiableList(names);
    }

    public Throwable getLastError()
    {
        Throwable paintError = lastPaintError;
        return paintError == null ? lastError : paintError;
    }

    public boolean isLegacyScript(Class<?> scriptClass)
    {
        return scriptClass != null && scriptClass.getClassLoader() instanceof LegacyScriptClassLoader
            && ((LegacyScriptClassLoader) scriptClass.getClassLoader()).isLegacyClass(scriptClass.getName());
    }

    /** Called by the event bus without allowing a faulty listener to stop client callbacks. */
    public void reportEventFailure(EventListener listener, Throwable error)
    {
        recordFailure("Event listener failed: " + listener.getClass().getName(),
            compatibilityFailure(listener.getClass(), error));
    }

    static Throwable compatibilityFailure(Class<?> scriptClass, Throwable error)
    {
        if (scriptClass == null || !(scriptClass.getClassLoader() instanceof LegacyScriptClassLoader))
            return error;
        Throwable linkage = error;
        while (linkage != null && !(linkage instanceof LinkageError)) linkage = linkage.getCause();
        if (linkage == null) return error;
        String detail = linkage.getMessage();
        if (detail == null || detail.trim().isEmpty()) detail = linkage.getClass().getSimpleName();
        detail = detail.replace('/', '.');
        return new LegacyScriptCompatibilityException(
            "Legacy script needs an API method, class, or dependency that is not compatible yet: "
                + detail, error);
    }

    /** Paints the active script into the game's main image buffer. */
    public void paint(Graphics2D graphics)
    {
        AbstractScript script = currentScript;
        if (script == null || state == State.STOP)
        {
            return;
        }
        Graphics2D copy = (Graphics2D) graphics.create();
        try
        {
            script.onPaint((Graphics) copy);
            randomManager.paint(copy);
        }
        catch (Throwable ex)
        {
            if (lastPaintError == null)
            {
                Throwable failure = compatibilityFailure(script.getClass(), ex);
                lastPaintError = failure;
                System.err.println("Script onPaint failed: " + failure.getMessage());
                failure.printStackTrace(System.err);
            }
        }
        finally
        {
            copy.dispose();
        }
    }

    public void shutdown()
    {
        stop();
        Thread thread = scriptThread;
        if (thread != null && thread != Thread.currentThread())
        {
            try
            {
                thread.join(2_000L);
            }
            catch (InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
        }
        synchronized (externalLoaders)
        {
            for (URLClassLoader loader : externalLoaders)
            {
                try
                {
                    loader.close();
                }
                catch (IOException ignored)
                {
                }
            }
            externalLoaders.clear();
        }
    }

    private void stopAndWaitForPrevious()
    {
        Thread previous = scriptThread;
        if (previous == Thread.currentThread())
        {
            throw new IllegalStateException("A script cannot replace itself from its own thread");
        }
        stop();
        if (previous == null)
        {
            return;
        }
        try
        {
            previous.join(2_000L);
        }
        catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while stopping the current script", ex);
        }
        if (previous.isAlive())
        {
            throw new IllegalStateException("The current script did not stop within two seconds");
        }
    }

    private boolean awaitRunnable(AbstractScript script) throws InterruptedException
    {
        synchronized (lifecycleLock)
        {
            while (currentScript == script && (state == State.PAUSED || state == State.SKIP))
            {
                try
                {
                    lifecycleLock.wait();
                }
                catch (InterruptedException ex)
                {
                    if (state == State.STOP || currentScript != script)
                    {
                        throw ex;
                    }
                }
            }
            return currentScript == script && state == State.RUNNING;
        }
    }

    private void waitAfterLoop(AbstractScript script, long delay) throws InterruptedException
    {
        if (delay <= 0L)
        {
            return;
        }
        synchronized (lifecycleLock)
        {
            if (currentScript == script && state == State.RUNNING)
            {
                try
                {
                    lifecycleLock.wait(delay);
                }
                catch (InterruptedException ex)
                {
                    if (state == State.STOP || currentScript != script)
                    {
                        throw ex;
                    }
                }
            }
        }
    }

    private void invokeLifecycle(String message, Runnable callback)
    {
        try
        {
            callback.run();
        }
        catch (Throwable ex)
        {
            recordFailure(message, ex);
        }
    }

    private void recordFailure(String message, Throwable error)
    {
        lastError = error;
        Logger.error(message + ": " + error.getMessage());
        error.printStackTrace(System.err);
    }
}
