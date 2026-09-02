package com.dreambotreborn.api.methods.login;

public final class LoginStats
{
    private static volatile int loginCount;
    private static volatile int gameState;
    private static volatile long loginTime;
    private static volatile long logoutTime;
    private static volatile boolean shouldProcess = true;
    private static volatile long lastLoginScreenStateTime;
    private static volatile long lastPacketReceived;
    private LoginStats() { }
    public static int getLoginCount() { return loginCount; }
    public static void setLoginCount(int value) { loginCount = Math.max(0, value); }
    public static int getGameState() { return gameState; }
    public static void setGameState(int value)
    {
        if (gameState != value && value == net.runelite.api.GameState.LOGIN_SCREEN.getState())
            lastLoginScreenStateTime = System.currentTimeMillis();
        gameState = value;
    }
    public static long getLoginTime() { return loginTime; }
    public static void setLoginTime(long value) { loginTime = value; }
    public static long getLogoutTime() { return logoutTime; }
    public static void setLogoutTime(long value) { logoutTime = value; }
    public static boolean isShouldProcess() { return shouldProcess; }
    public static void setShouldProcess(boolean value) { shouldProcess = value; }
    public static long getLastLoginScreenStateTime() { return lastLoginScreenStateTime; }
    public static long getLastPacketReceived() { return lastPacketReceived; }
    public static long getTimeBetweenLoginScreenAndLoginCompletion()
    {
        return loginTime <= 0 || lastLoginScreenStateTime <= 0 ? 0 : loginTime - lastLoginScreenStateTime;
    }
    public static int getAlwaysZeroMaybe() { return 0; }
    public static int getAlwaysZero1() { return 0; }
}
