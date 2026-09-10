package com.dreambotreborn.api.compat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Opt-in structural comparison against a locally supplied legacy client jar.
 * The reference binary is never bundled, copied, or initialized.
 */
class LegacyApiSurfaceAuditTest
{
    private static final String LEGACY_ROOT = "org.dreambot.api.";
    private static final String CURRENT_ROOT = "com.dreambotreborn.api.";

    @Test
    void reportLegacyTopLevelAndMethodCoverage() throws Exception
    {
        String configured = System.getProperty("dreambot.referenceJar", "").trim();
        Assumptions.assumeTrue(!configured.isEmpty(),
            "set -Ddreambot.referenceJar=/absolute/path/to/client.jar to run the audit");
        Path reference = Paths.get(configured).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isRegularFile(reference), "reference jar does not exist");

        List<String> legacyNames = apiClasses(reference);
        Set<String> absent = new TreeSet<>();
        int legacyPublicClasses = 0;
        int sharedClasses = 0;
        int legacyMethods = 0;
        int matchingMethods = 0;
        int skipped = 0;

        try (URLClassLoader loader = new URLClassLoader(
            new URL[] {reference.toUri().toURL()}, getClass().getClassLoader()))
        {
            for (String legacyName : legacyNames)
            {
                Class<?> legacy;
                try { legacy = Class.forName(legacyName, false, loader); }
                catch (LinkageError | ClassNotFoundException unreadable) { skipped++; continue; }
                if (!Modifier.isPublic(legacy.getModifiers())) continue;
                legacyPublicClasses++;
                String currentName = CURRENT_ROOT + legacyName.substring(LEGACY_ROOT.length());
                Class<?> current;
                try { current = Class.forName(currentName, false, getClass().getClassLoader()); }
                catch (LinkageError | ClassNotFoundException missing)
                {
                    absent.add(legacyName.substring(LEGACY_ROOT.length()));
                    continue;
                }
                sharedClasses++;
                Set<String> currentSignatures = publicSignatures(current);
                Set<String> referenceSignatures = publicSignatures(legacy);
                legacyMethods += referenceSignatures.size();
                for (String signature : referenceSignatures)
                    if (currentSignatures.contains(signature)) matchingMethods++;
            }
        }

        double classCoverage = legacyPublicClasses == 0 ? 0.0
            : sharedClasses * 100.0 / legacyPublicClasses;
        double methodCoverage = legacyMethods == 0 ? 0.0
            : matchingMethods * 100.0 / legacyMethods;
        System.out.printf("Legacy API audit: %d/%d public top-level classes (%.1f%%), "
                + "%d/%d public methods in shared classes (%.1f%%), %d skipped%n",
            sharedClasses, legacyPublicClasses, classCoverage,
            matchingMethods, legacyMethods, methodCoverage, skipped);
        if (!absent.isEmpty())
            System.out.println("Absent top-level APIs:\n  " + String.join("\n  ", absent));
    }

    private static List<String> apiClasses(Path path) throws IOException
    {
        List<String> result = new ArrayList<>();
        try (JarFile jar = new JarFile(path.toFile()))
        {
            java.util.Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements())
            {
                String name = entries.nextElement().getName();
                if (name.startsWith("org/dreambot/api/") && name.endsWith(".class")
                    && name.indexOf('$') < 0 && !name.equals("module-info.class"))
                    result.add(name.substring(0, name.length() - 6).replace('/', '.'));
            }
        }
        Collections.sort(result);
        return result;
    }

    private static Set<String> publicSignatures(Class<?> type)
    {
        Set<String> result = new HashSet<>();
        for (Method method : type.getMethods())
        {
            if (method.getDeclaringClass() == Object.class || method.isSynthetic() || method.isBridge()
                || !Modifier.isPublic(method.getModifiers()) || isObfuscated(method.getName())) continue;
            String parameters = java.util.Arrays.stream(method.getParameterTypes())
                .map(LegacyApiSurfaceAuditTest::normalize)
                .collect(Collectors.joining(","));
            result.add(method.getName() + "(" + parameters + ")->" + normalize(method.getReturnType()));
        }
        return result;
    }

    private static String normalize(Class<?> type)
    {
        if (type.isArray()) return normalize(type.getComponentType()) + "[]";
        return type.getName().replace(LEGACY_ROOT, CURRENT_ROOT);
    }

    private static boolean isObfuscated(String name)
    {
        return !name.matches("[A-Za-z_$][A-Za-z0-9_$]*") || name.matches("[0-9].*");
    }
}
