package com.dreambotreborn.api.methods.login;

import java.awt.Rectangle;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Client;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.methods.RSLoginResponse;
import com.dreambotreborn.api.methods.worldhopper.WorldHopper;
import com.dreambotreborn.api.utilities.Await;
import net.runelite.api.GameState;

/** Standalone login-screen helpers used by scripts and the account manager. */
public final class LoginUtility
{
    private static volatile boolean enterToLogin = true;
    private static volatile boolean escToCancel = true;
    private static volatile boolean rememberUsername;
    private static volatile boolean noInputLogin;
    private static volatile int hopWorld;
    private static volatile int worldListColumnSize = 20;
    private static volatile int baseX;
    private static volatile int baseY;
    private static volatile String loginPassword = "";
    private LoginUtility() { }
    public static boolean isEnterToLogin() { return enterToLogin; }
    public static void setEnterToLogin(boolean value) { enterToLogin = value; }
    public static void setEscToCancel(boolean value) { escToCancel = value; }
    public static boolean isEscToCancel() { return escToCancel; }
    public static String getLoginEmail() { return DreamBotRebornApi.requireClient().getUsername(); }
    public static String getLoginPassword() { return loginPassword; }
    public static LoginStage getStage() { return LoginStage.getStage(getIndex()); }
    public static Rectangle getExistingUserButton() { return button(30, 270); }
    public static RSLoginResponse login() { return login(getLoginEmail(), loginPassword); }
    public static RSLoginResponse login(String email, String password)
    {
        if (Client.isLoggedIn()) return RSLoginResponse.LOGGED_IN;
        if (email == null || password == null) return RSLoginResponse.INVALID_LOGIN;
        loginPassword = password;
        Await.result(ClientThread.invoke(() ->
        {
            DreamBotRebornApi.requireClient().setUsername(email);
            DreamBotRebornApi.requireClient().setPassword(password);
            return true;
        }), false);
        Rectangle login = getIndex() == 0 ? getExistingUserButton() : button(-130, 300);
        return Await.success(VirtualMouse.moveTo((int) login.getCenterX(), (int) login.getCenterY())
            .thenCompose(moved -> moved ? VirtualMouse.click(
                com.dreambotreborn.api.input.event.impl.mouse.MouseButton.LEFT_CLICK)
                : java.util.concurrent.CompletableFuture.completedFuture(false)))
            ? RSLoginResponse.CONNECTING_TO_SERVER : RSLoginResponse.FAILED_TO_LOGIN;
    }
    public static RSLoginResponse getResponse()
    {
        GameState state = Client.getGameState();
        if (state == GameState.LOGGED_IN) return RSLoginResponse.LOGGED_IN;
        if (state == GameState.LOGIN_SCREEN_AUTHENTICATOR) return RSLoginResponse.ENTER_AUTH;
        if (state == GameState.LOGGING_IN) return RSLoginResponse.CONNECTING_TO_SERVER;
        return RSLoginResponse.LOGIN_SCREEN;
    }
    public static int getIndex() { return Client.getLoginIndex(); }
    public static int getAccountStatus() { return Client.isLoggedIn() ? 1 : 0; }
    public static boolean isWorldScreenOpen() { return WorldHopper.isWorldHopperOpen(); }
    public static boolean openWorldScreen() { return WorldHopper.openWorldHopper(); }
    public static boolean closeWorldScreen() { return WorldHopper.closeWorldHopper(); }
    public static Rectangle getOpenWorldScreenButton() { return button(5, 465); }
    public static boolean changeWorld(int world) { hopWorld = world; return WorldHopper.hopWorld(world); }
    public static Rectangle getWorldRectangle(int world)
    {
        int index = getWorldIndex(world); if (index < 0) return null;
        return new Rectangle(baseX + (index / worldListColumnSize) * 190,
            baseY + (index % worldListColumnSize) * 24, 185, 22);
    }
    public static int getWorldIndex(int world)
    {
        java.util.List<com.dreambotreborn.api.methods.world.World> values =
            com.dreambotreborn.api.methods.world.Worlds.all().all();
        for (int i = 0; i < values.size(); i++) if (values.get(i).id == world) return i;
        return -1;
    }
    public static Rectangle getCancelLoginButton() { return button(-130, 300); }
    public static Rectangle getMembersBackButton() { return button(0, 320); }
    public static Rectangle getContinueButton() { return button(0, 320); }
    public static Rectangle getBackButtonOnLauncher() { return button(-80, 320); }
    public static Rectangle getAcceptTOSButton() { return button(80, 320); }
    public static Rectangle getTryAgainButton() { return button(0, 320); }
    public static Rectangle getBackButton() { return button(-80, 320); }
    public static Rectangle getDisabledBackButton() { return getBackButton(); }
    public static Rectangle getCancelWorldButton() { return button(0, 470); }
    public static Rectangle getRememberUsernameButton() { return button(-118, 263); }
    public static Rectangle getDisconnectedBackButton() { return getBackButton(); }
    public static Rectangle getPlayNowButton() { return button(0, 300); }
    public static Rectangle getPlayNowTryAgainButton() { return getPlayNowButton(); }
    public static Rectangle getMustAcceptTosBackButton() { return getBackButton(); }
    public static Rectangle getDobBackButton() { return getBackButton(); }
    public static int getWorldListColumnSize() { return worldListColumnSize; }
    public static void setWorldListColumnSize(int value) { worldListColumnSize = Math.max(1, value); }
    public static int getBaseX() { return baseX; }
    public static void setBaseX(int value) { baseX = value; }
    public static int getBaseY() { return baseY; }
    public static void setBaseY(int value) { baseY = value; }
    public static boolean isRememberUsername() { return rememberUsername; }
    public static void setRememberUsername(boolean value) { rememberUsername = value; }
    public static boolean isNoInputLogin() { return noInputLogin; }
    public static void setNoInputLogin(boolean value) { noInputLogin = value; }
    public static void enterUsername(String value)
    {
        ClientThread.invokeLater(() -> DreamBotRebornApi.requireClient().setUsername(value));
    }
    public static void enterPassword(String value)
    {
        loginPassword = value == null ? "" : value;
        ClientThread.invokeLater(() -> DreamBotRebornApi.requireClient().setPassword(loginPassword));
    }
    public static int getHopWorld() { return hopWorld; }
    public static void setHopWorld(int value) { hopWorld = value; }
    private static Rectangle button(int xOffset, int y)
    {
        int center = Math.max(765, DreamBotRebornApi.requireClient().getCanvasWidth()) / 2;
        return new Rectangle(center + xOffset - 50, y, 100, 42);
    }
}
