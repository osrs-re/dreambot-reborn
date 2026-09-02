package com.dreambotreborn.api.randoms;

import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Ordered solver registry evaluated before the active script's own loop. */
public final class RandomManager
{
    private final List<RandomSolver> solvers = new CopyOnWriteArrayList<>();
    private final LoginSolver loginSolver = new LoginSolver();
    private final BreakSolver breakSolver = new BreakSolver();
    private final WelcomeScreenSolver welcomeScreenSolver = new WelcomeScreenSolver();
    private volatile RandomSolver currentSolver;

    public RandomManager()
    {
        registerSolver(loginSolver);
        registerSolver(welcomeScreenSolver);
        registerSolver(new DismissSolver());
        registerSolver(breakSolver);
    }

    public RandomSolver getSolver(String name)
    {
        if (name == null) return null;
        for (RandomSolver solver : solvers)
            if (solver.getEventString().equalsIgnoreCase(name)) return solver;
        return null;
    }

    public void clearRegisteredSolvers()
    {
        finishCurrent();
        solvers.clear();
    }

    public void registerSolver(RandomSolver solver)
    {
        if (solver != null && !solvers.contains(solver)) solvers.add(solver);
    }

    public void unregisterSolver(RandomEvent event)
    {
        if (event != null) unregisterSolver(event.name());
    }

    public void unregisterSolver(String name)
    {
        RandomSolver solver = getSolver(name);
        if (solver != null)
        {
            if (currentSolver == solver) finishCurrent();
            solvers.remove(solver);
        }
    }

    /** Returns -1 when the ordinary script loop may run. */
    public int onLoop()
    {
        RandomSolver current = currentSolver;
        if (current != null && (!current.isEnabled() || !safeShouldExecute(current)))
        {
            finishCurrent();
            current = null;
        }
        if (current == null)
        {
            for (RandomSolver candidate : solvers)
            {
                if (candidate.isEnabled() && safeShouldExecute(candidate))
                {
                    com.dreambotreborn.api.script.AbstractScript script =
                        com.dreambotreborn.api.script.ScriptManager.getScriptManager().getCurrentScript();
                    if (script != null && !script.onSolverStart(candidate)) continue;
                    currentSolver = candidate;
                    current = candidate;
                    candidate.onStart();
                    break;
                }
            }
        }
        if (current == null) return -1;
        current.markRan();
        return Math.max(0, current.onLoop());
    }

    public boolean isSolving() { return currentSolver != null; }
    public BreakSolver getBreakSolver() { return breakSolver; }
    public LoginSolver getLoginSolver() { return loginSolver; }
    public WelcomeScreenSolver getWelcomeScreenSolver() { return welcomeScreenSolver; }
    public boolean isUsingCustomBreakSolver() { return false; }
    public RandomSolver getCurrentSolver() { return currentSolver; }

    public void disableSolver(String name)
    {
        RandomSolver solver = getSolver(name);
        if (solver != null) solver.disable();
    }

    public void disableSolver(RandomEvent event) { if (event != null) disableSolver(event.name()); }
    public void enableSolver(String name)
    {
        RandomSolver solver = getSolver(name);
        if (solver != null) solver.enable();
    }
    public void enableSolver(RandomEvent event) { if (event != null) enableSolver(event.name()); }

    public void paint(Graphics2D graphics)
    {
        RandomSolver current = currentSolver;
        if (current != null) current.onPaint(graphics);
    }

    public void reset()
    {
        finishCurrent();
        breakSolver.cancel();
        loginSolver.setLoginAction(null);
    }

    private void finishCurrent()
    {
        RandomSolver solver = currentSolver;
        currentSolver = null;
        if (solver != null)
        {
            try { solver.onFinish(); }
            catch (RuntimeException ignored) { }
            com.dreambotreborn.api.script.AbstractScript script =
                com.dreambotreborn.api.script.ScriptManager.getScriptManager().getCurrentScript();
            if (script != null)
            {
                try { script.onSolverEnd(solver); }
                catch (RuntimeException ignored) { }
            }
        }
    }

    private static boolean safeShouldExecute(RandomSolver solver)
    {
        try { return solver.shouldExecute(); }
        catch (RuntimeException ignored) { return false; }
    }
}
