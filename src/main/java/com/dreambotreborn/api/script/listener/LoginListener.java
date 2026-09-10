package com.dreambotreborn.api.script.listener;

import java.util.EventListener;

public interface LoginListener extends EventListener
{
    default void onLoginStageChange(int stage) { }
    default void onLoadingStateChange(int state) { }
    default void onLoginResponseChange(String first, String second, String third) { }
    default void onLoginResponse(int response) { }
}
