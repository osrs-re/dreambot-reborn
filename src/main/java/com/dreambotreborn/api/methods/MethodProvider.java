package com.dreambotreborn.api.methods;

import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.api.utilities.Sleep;
import com.dreambotreborn.api.utilities.impl.Condition;
import com.dreambotreborn.api.utilities.Logger;

/** Common helpers inherited by every script. */
public class MethodProvider
{
    public void sleep(long milliseconds)
    {
        Sleep.sleep(milliseconds);
    }

    public void sleep(long minimum, long maximum)
    {
        Sleep.sleep(minimum, maximum);
    }

    public boolean sleepUntil(Condition condition, long timeout)
    {
        return Sleep.sleepUntil(condition, timeout);
    }

    public boolean sleepUntil(Condition condition, long timeout, long poll)
    {
        return Sleep.sleepUntil(condition, timeout, poll);
    }

    public boolean sleepUntil(Condition condition, Condition resetCondition, long timeout, long poll)
    {
        return Sleep.sleepUntil(condition, resetCondition, timeout, poll);
    }

    public boolean sleepWhile(Condition condition, long timeout)
    {
        return Sleep.sleepWhile(condition, timeout);
    }

    public boolean sleepWhile(Condition condition, long timeout, long poll)
    {
        return Sleep.sleepWhile(condition, timeout, poll);
    }

    public boolean sleepWhile(Condition condition, Condition resetCondition, long timeout, long poll)
    {
        return Sleep.sleepWhile(condition, resetCondition, timeout, poll);
    }

    public void print(Object message)
    {
        log(message);
    }

    public void log(Object message)
    {
        Logger.info(message);
    }

    public void info(Object message)
    {
        log(message);
    }

    public void debug(Object message)
    {
        Logger.debug(message);
    }

    public void error(Object message)
    {
        Logger.error(message);
    }

    public void error(String message, Throwable cause)
    {
        Logger.error(message);
        if (cause != null)
        {
            cause.printStackTrace(System.err);
        }
    }

    public ScriptManager getScriptManager()
    {
        return ScriptManager.getScriptManager();
    }

    public boolean isUserVIP()
    {
        return false;
    }

    public boolean isUserSponsor()
    {
        return false;
    }
}
