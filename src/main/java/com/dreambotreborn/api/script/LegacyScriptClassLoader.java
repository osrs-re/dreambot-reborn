package com.dreambotreborn.api.script;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** External-script loader that remaps the old DreamBot API namespace in memory. */
final class LegacyScriptClassLoader extends URLClassLoader
{
    private final Set<String> legacyClasses =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    LegacyScriptClassLoader(URL[] urls, ClassLoader parent)
    {
        super(urls, parent);
    }

    boolean isLegacyClass(String className)
    {
        return legacyClasses.contains(className);
    }

    boolean loadedLegacyClasses()
    {
        return !legacyClasses.isEmpty();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        String resourceName = name.replace('.', '/') + ".class";
        URL resource = findResource(resourceName);
        if (resource == null) throw new ClassNotFoundException(name);
        try (InputStream input = resource.openStream())
        {
            LegacyBytecodeTransformer.Result result =
                LegacyBytecodeTransformer.transform(readAll(input), getParent());
            if (result.changed) legacyClasses.add(name);
            definePackageIfNeeded(name);
            return defineClass(name, result.bytecode, 0, result.bytecode.length);
        }
        catch (IOException error)
        {
            throw new ClassNotFoundException("Cannot load script class " + name, error);
        }
    }

    private void definePackageIfNeeded(String className)
    {
        int separator = className.lastIndexOf('.');
        if (separator < 0) return;
        String packageName = className.substring(0, separator);
        if (getDefinedPackage(packageName) != null) return;
        synchronized (this)
        {
            if (getDefinedPackage(packageName) == null)
            {
                try { definePackage(packageName, null, null, null, null, null, null, null); }
                catch (IllegalArgumentException alreadyDefined) { /* Concurrent class load. */ }
            }
        }
    }

    private static byte[] readAll(InputStream input) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int read; (read = input.read(buffer)) >= 0; )
            if (read > 0) output.write(buffer, 0, read);
        return output.toByteArray();
    }
}
