package com.dreambotreborn.accounts;

import java.awt.Rectangle;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import com.dreambotreborn.api.input.VirtualMouse;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

/** Game-thread state machine which switches to and logs into a selected account. */
public final class AccountLogin
{
    private static final long REQUEST_TIMEOUT_NANOS = 120_000_000_000L;
    private static final long ACTION_DELAY_NANOS = 700_000_000L;
    private static volatile Client client;
    private static volatile LoginRequest request;

    private AccountLogin()
    {
    }

    public static synchronized void initialize(Client value)
    {
        client = Objects.requireNonNull(value, "client");
        LoginRequest existing = request;
        request = null;
        if (existing != null)
        {
            existing.finish(AccountLoginResult.failure("Login cancelled because the client restarted"));
        }
    }

    static CompletableFuture<String> login(AccountCredentials credentials)
    {
        return ensureLoggedIn(credentials).thenApply(AccountLoginResult::getMessage);
    }

    static synchronized CompletableFuture<AccountLoginResult> ensureLoggedIn(
        AccountCredentials credentials)
    {
        Objects.requireNonNull(credentials, "credentials");
        if (client == null)
        {
            credentials.close();
            return CompletableFuture.completedFuture(
                AccountLoginResult.failure("The game client is not ready"));
        }
        if (request != null)
        {
            credentials.close();
            return CompletableFuture.completedFuture(
                AccountLoginResult.failure("Another automatic login is already running"));
        }
        LoginRequest created = new LoginRequest(credentials);
        request = created;
        return created.future;
    }

    /** Called from MinimalCallbacks on the game thread. */
    public static void tick()
    {
        LoginRequest current = request;
        Client currentClient = client;
        if (current == null || currentClient == null || current.actionInFlight
            || System.nanoTime() < current.nextActionNanos)
        {
            return;
        }
        if (System.nanoTime() >= current.deadlineNanos)
        {
            complete(current, AccountLoginResult.failure(
                "Timed out while switching to " + current.credentials.username));
            return;
        }

        GameState state = currentClient.getGameState();
        if (state == GameState.LOGGED_IN)
        {
            if (isSelectedAccount(currentClient, current.credentials.username))
            {
                complete(current, AccountLoginResult.success(
                    "Logged in as " + current.credentials.username));
            }
            else
            {
                logOutDifferentAccount(current, currentClient);
            }
            return;
        }
        if (state == GameState.LOGIN_SCREEN_AUTHENTICATOR)
        {
            complete(current, AccountLoginResult.failure(
                "Credentials accepted; enter the authenticator code, then start the script again"));
            return;
        }
        if (state == GameState.LOGGING_IN || state == GameState.LOADING
            || state == GameState.HOPPING)
        {
            current.sawLoginTransition = current.submitted || current.sawLoginTransition;
            return;
        }
        if (state == GameState.LOGIN_SCREEN)
        {
            logInSelectedAccount(current, currentClient);
            return;
        }
        if (state == GameState.STARTING || state == GameState.CONNECTION_LOST
            || state == GameState.UNKNOWN)
        {
            return;
        }
        complete(current, AccountLoginResult.failure(
            "The game client is not in a state that supports account switching: " + state));
    }

    private static boolean isSelectedAccount(Client currentClient, String selectedUsername)
    {
        String loggedInUsername = currentClient.getUsername();
        return loggedInUsername != null
            && loggedInUsername.trim().equalsIgnoreCase(selectedUsername.trim());
    }

    private static void logOutDifferentAccount(LoginRequest current, Client currentClient)
    {
        Widget logout = currentClient.getWidget(InterfaceID.Logout.LOGOUT);
        if (isClickable(logout))
        {
            moveAndClick(current, logout.getBounds(), false);
            return;
        }

        Widget logoutTab = firstClickable(
            currentClient.getWidget(ComponentID.FIXED_VIEWPORT_LOGOUT_TAB),
            currentClient.getWidget(ComponentID.RESIZABLE_VIEWPORT_LOGOUT_TAB),
            currentClient.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_MINIMAP_LOGOUT_BUTTON));
        if (logoutTab != null)
        {
            moveAndClick(current, logoutTab.getBounds(), false);
            return;
        }

        complete(current, AccountLoginResult.failure(
            "Another account is logged in, but the logout control is not available"));
    }

    private static void logInSelectedAccount(LoginRequest current, Client currentClient)
    {
        if (current.submitted && current.sawLoginTransition)
        {
            complete(current, AccountLoginResult.failure(
                "RuneScape returned to the login screen; verify the selected account credentials"));
            return;
        }
        if (current.submitted && System.nanoTime() - current.submittedAtNanos > 10_000_000_000L)
        {
            complete(current, AccountLoginResult.failure(
                "RuneScape did not accept the login submission"));
            return;
        }

        int loginIndex = currentClient.getLoginIndex();
        int centerX = Math.max(765, currentClient.getCanvasWidth()) / 2;
        if (loginIndex == 0)
        {
            moveAndClick(current, new Rectangle(centerX + 30, 270, 100, 42), false);
        }
        else if (loginIndex == 2 && !current.submitted)
        {
            currentClient.setUsername(current.credentials.username);
            currentClient.setPassword(new String(current.credentials.password));
            current.submitted = true;
            current.submittedAtNanos = System.nanoTime();
            moveAndClick(current, new Rectangle(centerX - 130, 300, 100, 42), true);
        }
        else if (loginIndex != 2)
        {
            complete(current, AccountLoginResult.failure(
                "Open the username/password form before using automatic login"));
        }
    }

    private static Widget firstClickable(Widget... widgets)
    {
        for (Widget widget : widgets)
        {
            if (isClickable(widget))
            {
                return widget;
            }
        }
        return null;
    }

    private static boolean isClickable(Widget widget)
    {
        Rectangle bounds = widget == null ? null : widget.getBounds();
        return widget != null && !widget.isHidden() && bounds != null
            && bounds.width > 0 && bounds.height > 0;
    }

    private static void moveAndClick(
        LoginRequest current,
        Rectangle bounds,
        boolean loginSubmission)
    {
        int x = bounds.x + bounds.width / 2;
        int y = bounds.y + bounds.height / 2;
        current.actionInFlight = true;
        VirtualMouse.moveTo(x, y).whenComplete((moved, error) ->
        {
            if (request != current)
            {
                return;
            }
            if (error != null || !Boolean.TRUE.equals(moved))
            {
                complete(current, AccountLoginResult.failure(
                    "Unable to move the virtual mouse to an account control"));
                return;
            }
            VirtualMouse.click();
            if (loginSubmission)
            {
                current.submitted = true;
            }
            current.nextActionNanos = System.nanoTime() + ACTION_DELAY_NANOS;
            current.actionInFlight = false;
        });
    }

    private static synchronized void complete(
        LoginRequest expected,
        AccountLoginResult result)
    {
        if (request != expected)
        {
            return;
        }
        request = null;
        expected.finish(result);
    }

    private static final class LoginRequest
    {
        private final AccountCredentials credentials;
        private final CompletableFuture<AccountLoginResult> future = new CompletableFuture<>();
        private final long deadlineNanos = System.nanoTime() + REQUEST_TIMEOUT_NANOS;
        private volatile boolean actionInFlight;
        private volatile boolean submitted;
        private volatile boolean sawLoginTransition;
        private volatile long submittedAtNanos;
        private volatile long nextActionNanos;

        private LoginRequest(AccountCredentials credentials)
        {
            this.credentials = credentials;
        }

        private void finish(AccountLoginResult result)
        {
            credentials.close();
            future.complete(result);
        }
    }
}
