package com.dreambotreborn.api.script;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Class-path and external-jar discovery for {@link ScriptManifest} scripts. */
final class ScriptLoader
{
    private static final String PACKAGE_PATH = "com/dreambotreborn";

    private ScriptLoader()
    {
    }

    static Discovery discover(Path scriptsDirectory)
    {
        List<ScriptDescriptor> scripts = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<URLClassLoader> classLoaders = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        ClassLoader applicationLoader = AbstractScript.class.getClassLoader();

        scanApplicationClassPath(applicationLoader, scripts, errors, seen);
        try
        {
            Files.createDirectories(scriptsDirectory);
            List<Path> jars;
            try (Stream<Path> files = Files.list(scriptsDirectory))
            {
                jars = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .sorted()
                    .collect(Collectors.toList());
            }

            if (!jars.isEmpty())
            {
                URL[] urls = new URL[jars.size()];
                for (int index = 0; index < jars.size(); index++)
                {
                    urls[index] = jars.get(index).toUri().toURL();
                }
                URLClassLoader loader = new LegacyScriptClassLoader(urls, applicationLoader);
                classLoaders.add(loader);
                for (Path jar : jars)
                {
                    scanJar(jar, loader, scripts, errors, seen, true);
                }
            }
        }
        catch (IOException ex)
        {
            errors.add("Cannot scan " + scriptsDirectory + ": " + ex.getMessage());
        }

        scripts.sort(Comparator
            .comparing((ScriptDescriptor script) -> script.getManifest().name(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(script -> script.getManifest().author(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(ScriptDescriptor::getClassName));
        return new Discovery(scripts, errors, classLoaders);
    }

    private static void scanApplicationClassPath(
        ClassLoader loader,
        List<ScriptDescriptor> scripts,
        List<String> errors,
        Set<String> seen)
    {
        // Surefire, IDE launchers and modular runtimes do not always expose the
        // application's output directory through java.class.path. The code
        // source is the authoritative location for DreamBot Reborn's bundled scripts.
        try
        {
            URL codeSource = AbstractScript.class.getProtectionDomain()
                .getCodeSource()
                .getLocation();
            if ("file".equalsIgnoreCase(codeSource.getProtocol()))
            {
                Path entry = Paths.get(codeSource.toURI()).toAbsolutePath().normalize();
                scanApplicationEntry(entry, loader, scripts, errors, seen);
            }
        }
        catch (Exception | LinkageError ex)
        {
            errors.add("Cannot locate application classes: " + ex.getMessage());
        }

        String classPath = System.getProperty("java.class.path", "");
        for (String value : classPath.split(java.util.regex.Pattern.quote(File.pathSeparator)))
        {
            if (value.isEmpty())
            {
                continue;
            }
            Path entry = Paths.get(value).toAbsolutePath().normalize();
            scanApplicationEntry(entry, loader, scripts, errors, seen);
        }
    }

    private static void scanApplicationEntry(
        Path entry,
        ClassLoader loader,
        List<ScriptDescriptor> scripts,
        List<String> errors,
        Set<String> seen)
    {
        if (Files.isDirectory(entry))
        {
            scanDirectory(entry, loader, scripts, errors, seen);
        }
        else if (entry.getFileName() != null
            && entry.getFileName().toString().toLowerCase(Locale.ROOT).startsWith("dreambot-reborn")
            && entry.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
        {
            scanJar(entry, loader, scripts, errors, seen, false);
        }
    }

    private static void scanDirectory(
        Path classPathRoot,
        ClassLoader loader,
        List<ScriptDescriptor> scripts,
        List<String> errors,
        Set<String> seen)
    {
        Path packageRoot = classPathRoot.resolve(PACKAGE_PATH);
        if (!Files.isDirectory(packageRoot))
        {
            return;
        }
        try (Stream<Path> files = Files.walk(packageRoot))
        {
            files.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".class"))
                .map(path -> classPathRoot.relativize(path).toString())
                .map(ScriptLoader::className)
                .filter(name -> name.indexOf('$') < 0)
                .forEach(name -> inspect(name, loader, classPathRoot, scripts, errors, seen, false));
        }
        catch (IOException ex)
        {
            errors.add("Cannot scan " + classPathRoot + ": " + ex.getMessage());
        }
    }

    private static void scanJar(
        Path jarPath,
        ClassLoader loader,
        List<ScriptDescriptor> scripts,
        List<String> errors,
        Set<String> seen,
        boolean external)
    {
        try (JarFile jar = new JarFile(jarPath.toFile()))
        {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements())
            {
                String name = entries.nextElement().getName();
                if (!name.endsWith(".class") || name.indexOf('$') >= 0)
                {
                    continue;
                }
                // A script JAR may accidentally bundle the old or current API.
                // Those definitions must never shadow the application's API.
                if (external && (name.startsWith("org/dreambot/api/")
                    || name.startsWith("com/dreambotreborn/api/")))
                {
                    continue;
                }
                if (!external && !name.startsWith(PACKAGE_PATH + "/"))
                {
                    continue;
                }
                inspect(className(name), loader, jarPath, scripts, errors, seen, external);
            }
        }
        catch (IOException ex)
        {
            errors.add("Cannot read " + jarPath.getFileName() + ": " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void inspect(
        String className,
        ClassLoader loader,
        Path source,
        List<ScriptDescriptor> scripts,
        List<String> errors,
        Set<String> seen,
        boolean reportLoadFailures)
    {
        if (seen.contains(className))
        {
            return;
        }
        try
        {
            Class<?> candidate = Class.forName(className, false, loader);
            if (candidate == AbstractScript.class
                || !AbstractScript.class.isAssignableFrom(candidate)
                || java.lang.reflect.Modifier.isAbstract(candidate.getModifiers()))
            {
                return;
            }
            ScriptManifest manifest = candidate.getAnnotation(ScriptManifest.class);
            if (manifest == null)
            {
                return;
            }
            candidate.getDeclaredConstructor();
            seen.add(className);
            scripts.add(new ScriptDescriptor(
                (Class<? extends AbstractScript>) candidate, manifest, source,
                loader instanceof LegacyScriptClassLoader
                    && ((LegacyScriptClassLoader) loader).isLegacyClass(className)));
        }
        catch (ReflectiveOperationException | LinkageError | SecurityException ex)
        {
            if (reportLoadFailures)
            {
                errors.add(className + ": " + ex.getClass().getSimpleName() + " — " + ex.getMessage());
            }
        }
    }

    private static String className(String path)
    {
        String normalized = path.replace(File.separatorChar, '.').replace('/', '.');
        return normalized.substring(0, normalized.length() - ".class".length());
    }

    static final class Discovery
    {
        final List<ScriptDescriptor> scripts;
        final List<String> errors;
        final List<URLClassLoader> classLoaders;

        Discovery(
            List<ScriptDescriptor> scripts,
            List<String> errors,
            List<URLClassLoader> classLoaders)
        {
            this.scripts = scripts;
            this.errors = errors;
            this.classLoaders = classLoaders;
        }
    }
}
