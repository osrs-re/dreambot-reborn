package com.dreambotreborn.api.randoms;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import com.dreambotreborn.api.Client;
import com.dreambotreborn.api.data.GameState;

/** Restarts the selected account's login flow if a running script is logged out. */
public class LoginSolver extends RandomSolver
{
    private volatile Supplier<CompletableFuture<Boolean>> loginAction;
    private volatile CompletableFuture<Boolean> inFlight;

    public LoginSolver()
    {
        super(RandomEvent.LOGIN);
    }

    public void setLoginAction(Supplier<CompletableFuture<Boolean>> action)
    {
        loginAction = action;
        inFlight = null;
    }

    @Override
    public boolean shouldExecute()
    {
        CompletableFuture<Boolean> current = inFlight;
        if (current != null && !current.isDone()) return true;
        GameState state;
        try { state = Client.getGameState(); }
        catch (RuntimeException ignored) { return false; }
        return loginAction != null && state != GameState.LOGGED_IN;
    }

    @Override
    public int onLoop()
    {
        CompletableFuture<Boolean> current = inFlight;
        if (current == null)
        {
            Supplier<CompletableFuture<Boolean>> action = loginAction;
            if (action == null) return 600;
            try { inFlight = action.get(); }
            catch (RuntimeException ignored) { return 1_000; }
            return 600;
        }
        if (current.isDone()) inFlight = null;
        return 600;
    }

    @Override public String getDebugInfo() { return inFlight == null ? "waiting to log in" : "login in progress"; }
}
