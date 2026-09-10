package com.dreambotreborn.api.script;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable metadata for a script discovered by the manager. */
public final class ScriptDescriptor
{
    private final Class<? extends AbstractScript> scriptClass;
    private final ScriptManifest manifest;
    private final Path source;
    private final boolean legacy;

    ScriptDescriptor(
        Class<? extends AbstractScript> scriptClass,
        ScriptManifest manifest,
        Path source,
        boolean legacy)
    {
        this.scriptClass = Objects.requireNonNull(scriptClass, "scriptClass");
        this.manifest = Objects.requireNonNull(manifest, "manifest");
        this.source = source;
        this.legacy = legacy;
    }

    public Class<? extends AbstractScript> getScriptClass()
    {
        return scriptClass;
    }

    public ScriptManifest getManifest()
    {
        return manifest;
    }

    public Path getSource()
    {
        return source;
    }

    /** True when the script was compiled against the old org.dreambot.api namespace. */
    public boolean isLegacy()
    {
        return legacy;
    }

    public String getClassName()
    {
        return scriptClass.getName();
    }

    @Override
    public String toString()
    {
        return manifest.name();
    }
}
