package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.LoginListener;

public class LoginResponseEvent extends ScriptEvent
{
    private final Integer response;
    private final String first, second, third;
    public LoginResponseEvent(int response)
    { this.response = response; this.first = this.second = this.third = null; }
    public LoginResponseEvent(String first, String second, String third)
    { this.response = null; this.first = first; this.second = second; this.third = third; }
    @Override public void dispatch(EventListener listener)
    {
        if (!(listener instanceof LoginListener)) return;
        if (response != null) ((LoginListener) listener).onLoginResponse(response);
        else ((LoginListener) listener).onLoginResponseChange(first, second, third);
    }
}
