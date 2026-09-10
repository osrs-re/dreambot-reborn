package com.dreambotreborn.api.data;

/** DreamBot-compatible view of the underlying RuneScape client state. */
public enum GameState
{
    NULL(-1),
    LOGIN_SCREEN(0),
    ENTER_AUTH(11),
    LOGGING_IN(3),
    INITIAL_SPLASH_SCREEN(6),
    LOGGED_IN(10),
    LOADING(-1),
    HOPPING(-1),
    CRASHED(-1),
    GAME_LOADING(5);

    private final int id;

    GameState(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static GameState fromRuneLite(net.runelite.api.GameState state)
    {
        if (state == null) return NULL;
        switch (state)
        {
            case STARTING: return INITIAL_SPLASH_SCREEN;
            case LOGIN_SCREEN: return LOGIN_SCREEN;
            case LOGIN_SCREEN_AUTHENTICATOR: return ENTER_AUTH;
            case LOGGING_IN: return LOGGING_IN;
            case LOADING: return LOADING;
            case LOGGED_IN: return LOGGED_IN;
            case CONNECTION_LOST: return CRASHED;
            case HOPPING: return HOPPING;
            case UNKNOWN:
            default: return NULL;
        }
    }
}
