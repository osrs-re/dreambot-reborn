package com.dreambotreborn.api.script;

/** Raised when a remapped legacy script calls an API or dependency that is unavailable. */
public final class LegacyScriptCompatibilityException extends RuntimeException
{
    public LegacyScriptCompatibilityException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
