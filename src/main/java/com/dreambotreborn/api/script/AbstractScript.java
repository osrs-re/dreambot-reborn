package com.dreambotreborn.api.script;

import java.awt.Graphics;
import java.awt.Graphics2D;
import com.dreambotreborn.api.methods.MethodProvider;
import com.dreambotreborn.api.script.listener.PaintListener;
import com.dreambotreborn.api.script.frameworks.utility.Loggable;
import com.dreambotreborn.api.script.frameworks.utility.Sleepable;
import com.dreambotreborn.api.utilities.Timer;
import com.dreambotreborn.api.randoms.RandomManager;
import com.dreambotreborn.api.randoms.RandomSolver;

/** Base class for managed DreamBot Reborn scripts. */
public abstract class AbstractScript extends MethodProvider
    implements Runnable, PaintListener, Sleepable, Loggable
{
    private final Timer scriptTimer = new Timer();
    private volatile ScriptManager manager;
    private volatile String[] arguments = new String[0];

    public void onStart()
    {
    }

    public void onStart(String... args)
    {
        onStart();
    }

    public abstract int onLoop();

    /** Return false to decline a random solver for this script session. */
    public boolean onSolverStart(RandomSolver solver)
    {
        return true;
    }

    public void onSolverEnd(RandomSolver solver)
    {
    }

    public boolean onScheduledStop()
    {
        return true;
    }

    public void onExit()
    {
    }

    public void onPause()
    {
    }

    public void onResume()
    {
    }

    @Override
    public void onPaint(Graphics graphics)
    {
        if (graphics instanceof Graphics2D)
        {
            onPaint((Graphics2D) graphics);
        }
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
    }

    public void stop()
    {
        getScriptManager().stop();
    }

    public void stop(boolean logout)
    {
        getScriptManager().stop(logout);
    }

    public boolean isPaused()
    {
        return getScriptManager().isPaused();
    }

    public void setState(ScriptManager.State state)
    {
        getScriptManager().setState(this, state);
    }

    @Override
    public final void run()
    {
        getScriptManager().execute(this);
    }

    @Override
    public ScriptManager getScriptManager()
    {
        ScriptManager current = manager;
        return current == null ? ScriptManager.getScriptManager() : current;
    }

    public ScriptManager.State getCurrentState()
    {
        return getScriptManager().getState();
    }

    public Timer getScriptTimer()
    {
        return scriptTimer;
    }

    public final void buildRandomManager()
    {
        getRandomManager();
    }

    public RandomManager getRandomManager()
    {
        return getScriptManager().getRandomManager();
    }

    public Thread getRandomThread()
    {
        return null;
    }

    public boolean isUserVIP() { return false; }
    public boolean isUserSponsor() { return false; }

    public final ScriptManifest getManifest()
    {
        return getClass().getAnnotation(ScriptManifest.class);
    }

    public double getVersion()
    {
        ScriptManifest manifest = getManifest();
        return manifest == null ? 0.0D : manifest.version();
    }

    public final String getSDNName()
    {
        ScriptManifest manifest = getManifest();
        return manifest == null ? "" : manifest.name();
    }

    public final String getThreadURL()
    {
        return "";
    }

    public final int getScriptId()
    {
        return -1;
    }

    public final int getStoreId()
    {
        return -1;
    }

    public final String getSDNParameters()
    {
        return "";
    }

    final void bind(ScriptManager manager, String[] arguments)
    {
        this.manager = manager;
        this.arguments = arguments.clone();
    }

    final String[] arguments()
    {
        return arguments.clone();
    }
}
