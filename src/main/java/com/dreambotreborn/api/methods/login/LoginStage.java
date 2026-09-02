package com.dreambotreborn.api.methods.login;

import com.dreambotreborn.api.Client;
import net.runelite.api.GameState;

public enum LoginStage
{
    LOGIN_SCREEN, SOMETHING1, ENTER_INFO, INVALID_INFORMATION, AUTH,
    FORGOTTEN_PASSWORD, UPDATE_DOB, WORLD_SCREEN, PROCESSING,
    LOGIN_SCREEN_PLAY_NOW, DISABLED, LOCKED, MUST_ACCEPT_TOS, DISCONNECTED,
    TOS_AGREEMENT, ENTER_DOB, LAUNCHER, MEMBERS;

    public static LoginStage getStage(int index)
    {
        if (Client.getGameState() == GameState.LOGIN_SCREEN_AUTHENTICATOR) return AUTH;
        switch (index)
        {
            case 0: return LOGIN_SCREEN;
            case 2: return ENTER_INFO;
            case 3: return INVALID_INFORMATION;
            case 4: return AUTH;
            case 5: return FORGOTTEN_PASSWORD;
            case 7: return UPDATE_DOB;
            case 10: return LOGIN_SCREEN_PLAY_NOW;
            case 12: return DISABLED;
            case 14: return MUST_ACCEPT_TOS;
            case 24: return ENTER_DOB;
            default: return PROCESSING;
        }
    }
}
