package com.dreambotreborn.api.script.frameworks.utility;

import java.awt.Color;

/** Formatting and logging mixin matching DreamBot's script framework. */
public interface Loggable
{
    default void print(Object message)
    {
        log(message);
    }

    default void log(Object message)
    {
        System.out.println("[Script] " + String.valueOf(message));
    }

    default void info(Object message)
    {
        log(message);
    }

    default void debug(Object message)
    {
        System.out.println("[Script/debug] " + String.valueOf(message));
    }

    default void error(Object message)
    {
        System.err.println("[Script] " + String.valueOf(message));
    }

    default void log(String format, Object... arguments)
    {
        log(format(format, arguments));
    }

    default void log(Color color, String format, Object... arguments)
    {
        log(format(format, arguments));
    }

    default void info(String format, Object... arguments)
    {
        info(format(format, arguments));
    }

    default void debug(String format, Object... arguments)
    {
        debug(format(format, arguments));
    }

    default void warn(String format, Object... arguments)
    {
        System.err.println("[Script/warn] " + format(format, arguments));
    }

    default void error(String format, Object... arguments)
    {
        error(format(format, arguments));
    }

    default void error(String format, Throwable cause, Object... arguments)
    {
        System.err.println("[Script] " + format(format, arguments));
        if (cause != null)
        {
            cause.printStackTrace(System.err);
        }
    }

    static String format(String value, Object... arguments)
    {
        if (arguments == null || arguments.length == 0)
        {
            return value;
        }
        return String.format(value.replace("{}", "%s"), arguments);
    }
}
