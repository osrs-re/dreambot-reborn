package com.dreambotreborn.api.script;

import java.nio.file.Path;
import java.util.Objects;

/** Immutable metadata for a script discovered by the manager. */
public final class ScriptDescriptor
{
    private final Class<? extends AbstractScript> scriptClass;
    private final ScriptManifest manifest;
    private final Path source;

    ScriptDescriptor(
        Class<? extends AbstractScript> scriptClass,
        ScriptManifest manifest,
        Path source)
    {
        this.scriptClass = Objects.requireNonNull(scriptClass, "scriptClass");
        this.manifest = Objects.requireNonNull(manifest, "manifest");
        this.source = source;
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
