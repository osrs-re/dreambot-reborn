package com.dreambotreborn.api.methods;

import java.util.Locale;

public enum RSLoginResponse
{
    CONNECTION_TIMED_OUT, ERROR_CONNECTING, FAILED_TO_LOGIN, NO_RESPONSE,
    NOT_LOGGED_OUT, STILL_LOGGED_IN, INVALID_LOGIN, DISABLED, ALREADY_LOGGED_IN,
    UPDATED, FULL_WORLD, UNABLE_TO_CONNECT, LOGIN_LIMIT_EXCEEDED, BAD_SESSION,
    PASSWORD_KNOWN, MEMBERS_WORLD, MEMBERS_WORLD_2, FAILED_TO_COMPLETE_LOGIN,
    SERVER_UPDATED, SERVER_UPDATING, TOO_MANY_ATTEMPTS, TOO_MANY_ATTEMPTS_LAUNCHER,
    MEMBERS_AREA, ACCOUNT_LOCKED, CLOSED_BETA, INVALID_LOGIN_SERVER, MALFORMED_PACKET,
    NO_REPLY, ERROR_LOADING_PROFILE, UNEXPECTED_LOGIN_RESPONSE, ADDRESS_BLOCKED,
    SERVICE_UNAVAILABLE, SET_DISPLAY_NAME, UNSUCCESSFUL_LOGIN, INACCESSIBLE, VOTE,
    NOT_ELIGIBLE, ENTER_AUTH, BAD_AUTH_CODE, UNEXPECTED_SERVER_RESPONSE,
    CONNECTING_TO_SERVER, TOTAL_LEVEL, WORLD_LOCKED, LOGIN_SCREEN, LOGGED_IN, NOT_FOUND;

    public String[] getResponses() { return new String[] {name().replace('_', ' ')}; }
    public int getSeverity() { return this == LOGGED_IN || this == CONNECTING_TO_SERVER ? 0 : 1; }
    public static RSLoginResponse getLoginResponse(String... messages)
    {
        String joined = messages == null ? "" : String.join(" ", messages).toLowerCase(Locale.ENGLISH);
        if (joined.contains("authenticator")) return ENTER_AUTH;
        if (joined.contains("invalid") || joined.contains("incorrect")) return INVALID_LOGIN;
        if (joined.contains("too many")) return TOO_MANY_ATTEMPTS;
        if (joined.contains("disabled")) return DISABLED;
        if (joined.contains("full")) return FULL_WORLD;
        if (joined.contains("connecting")) return CONNECTING_TO_SERVER;
        return joined.isEmpty() ? NO_RESPONSE : NOT_FOUND;
    }
}
