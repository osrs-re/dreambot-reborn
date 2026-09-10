package com.dreambotreborn.api.script;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyScriptLoaderTest
{
    @AfterEach
    void closeLoaders()
    {
        ScriptManager.getScriptManager().shutdown();
    }

    @Test
    void discoversAndExecutesAnUnmodifiedOrgDreambotScriptJar(@TempDir Path directory)
        throws Exception
    {
        Path source = directory.resolve("source");
        Path classes = directory.resolve("classes");
        Path scripts = directory.resolve("scripts");
        Files.createDirectories(source);
        Files.createDirectories(classes);
        Files.createDirectories(scripts);
        List<Path> sources = Arrays.asList(
            java(source, "org/dreambot/api/script/Category.java",
                "package org.dreambot.api.script; public enum Category { UTILITY }"),
            java(source, "org/dreambot/api/script/ScriptManifest.java",
                "package org.dreambot.api.script;"
                    + "import java.lang.annotation.*;"
                    + "@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)"
                    + "public @interface ScriptManifest { Category category(); String name();"
                    + "String description() default \"\"; String author(); double version();"
                    + "String image() default \"\"; String _key() default \"\"; }"),
            java(source, "org/dreambot/api/script/AbstractScript.java",
                "package org.dreambot.api.script;"
                    + "public abstract class AbstractScript { public abstract int onLoop(); }"),
            java(source, "org/dreambot/api/data/GameState.java",
                "package org.dreambot.api.data; public enum GameState { LOGIN_SCREEN }"),
            java(source, "org/dreambot/api/methods/Calculations.java",
                "package org.dreambot.api.methods; public final class Calculations {"
                    + "public static int random(int minimum, int maximum) { return minimum; } }"),
            java(source, "org/dreambot/api/methods/filter/Filter.java",
                "package org.dreambot.api.methods.filter; @FunctionalInterface "
                    + "public interface Filter<T> { boolean match(T value); }"),
            java(source, "org/dreambot/api/methods/interactive/GameObjects.java",
                "package org.dreambot.api.methods.interactive; import java.util.*;"
                    + "import org.dreambot.api.methods.filter.Filter;"
                    + "public final class GameObjects { public static List<Object> all() {"
                    + "return Collections.emptyList(); } public static List<Object> all("
                    + "Integer... ids) { return Collections.emptyList(); } public static List<Object> find("
                    + "Filter<Object> filter) { return Collections.emptyList(); } }"),
            java(source, "org/dreambot/api/future/NotPorted.java",
                "package org.dreambot.api.future; public final class NotPorted {"
                    + "public static int call() { return 123; } }"),
            java(source, "legacy/fixture/LegacyFixtureScript.java",
                "package legacy.fixture;"
                    + "import java.util.function.Supplier;"
                    + "import org.dreambot.api.data.GameState;"
                    + "import org.dreambot.api.methods.Calculations;"
                    + "import org.dreambot.api.future.NotPorted;"
                    + "import org.dreambot.api.script.*;"
                    + "@ScriptManifest(name=\"Old Script\",author=\"Fixture\","
                    + "category=Category.UTILITY,version=1.2)"
                    + "public final class LegacyFixtureScript extends AbstractScript {"
                    + "public int onLoop(){ return NotPorted.call(); }"
                    + "public String remappedPackage(){ Supplier<String> value = () -> "
                    + "GameState.LOGIN_SCREEN.getClass().getPackage().getName(); return value.get(); }"
                    + "public int compatibleMethodCall(){ return Calculations.random(4, 5); }"
                    + "public int covariantReturnCall(){ return "
                    + "org.dreambot.api.methods.interactive.GameObjects.all().size(); }"
                    + "public int adaptedFilterCall(){ return "
                    + "org.dreambot.api.methods.interactive.GameObjects.find(value -> true).size(); }"
                    + "public int boxedIdCall(){ return "
                    + "org.dreambot.api.methods.interactive.GameObjects.all(42).size(); } }"));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests require a JDK, not a JRE");
        java.util.List<String> arguments = new java.util.ArrayList<>();
        arguments.addAll(Arrays.asList("--release", "11", "-d", classes.toString()));
        for (Path path : sources) arguments.add(path.toString());
        assertEquals(0, compiler.run(null, null, null, arguments.toArray(new String[0])));

        Path jar = scripts.resolve("untouched-old-script.jar");
        addToJar(jar, classes, "legacy/fixture/LegacyFixtureScript.class");
        byte[] originalJar = Files.readAllBytes(jar);
        ScriptManager manager = ScriptManager.getScriptManager();
        manager.initialize(scripts);

        ScriptDescriptor descriptor = manager.getDiscoveredScripts().stream()
            .filter(candidate -> candidate.getManifest().name().equals("Old Script"))
            .findFirst().orElseThrow(() -> new AssertionError(manager.getDiscoveryErrors()));
        assertTrue(descriptor.isLegacy());
        assertTrue(AbstractScript.class.isAssignableFrom(descriptor.getScriptClass()));
        Object script = descriptor.getScriptClass().getDeclaredConstructor().newInstance();
        assertEquals("com.dreambotreborn.api.data",
            descriptor.getScriptClass().getMethod("remappedPackage").invoke(script));
        assertEquals(4, descriptor.getScriptClass().getMethod("compatibleMethodCall").invoke(script));
        assertEquals(0, descriptor.getScriptClass().getMethod("covariantReturnCall").invoke(script),
            "A compiled List return descriptor must adapt to the current Query return type");
        assertEquals(0, descriptor.getScriptClass().getMethod("adaptedFilterCall").invoke(script),
            "Legacy Filter parameters must adapt to current Predicate parameters");
        assertEquals(0, descriptor.getScriptClass().getMethod("boxedIdCall").invoke(script),
            "DreamBot's boxed ID varargs descriptor must remain callable");

        Throwable diagnostic = ScriptManager.compatibilityFailure(descriptor.getScriptClass(),
            new NoClassDefFoundError("com/dreambotreborn/api/future/NotPorted"));
        assertTrue(diagnostic instanceof LegacyScriptCompatibilityException,
            String.valueOf(diagnostic));
        assertTrue(diagnostic.getMessage().contains("not compatible yet"));
        assertArrayEquals(originalJar, Files.readAllBytes(jar),
            "Compatibility loading must never rewrite the user's script JAR");
    }

    @Test
    void loadsAClassCompiledAgainstTheLocallyOwnedReferenceJar(@TempDir Path directory)
        throws Exception
    {
        String configured = System.getProperty("dreambot.referenceJar", "").trim();
        Assumptions.assumeTrue(!configured.isEmpty(),
            "set -Ddreambot.referenceJar=/absolute/path/to/client.jar for the real-JAR test");
        Path reference = java.nio.file.Paths.get(configured).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isRegularFile(reference), "reference jar does not exist");

        Path source = directory.resolve("source");
        Path classes = directory.resolve("classes");
        Path scripts = directory.resolve("scripts");
        Files.createDirectories(classes);
        Files.createDirectories(scripts);
        Path scriptSource = java(source, "legacy/reference/ReferenceCompiledScript.java",
            "package legacy.reference;"
                + "import org.dreambot.api.methods.interactive.GameObjects;"
                + "import org.dreambot.api.script.*;"
                + "import org.dreambot.api.wrappers.interactive.GameObject;"
                + "import java.awt.Graphics;"
                + "@ScriptManifest(name=\"Reference compiled\",author=\"Fixture\","
                + "category=Category.UTILITY,version=1.0)"
                + "public final class ReferenceCompiledScript extends AbstractScript {"
                + "private volatile boolean painted;"
                + "public int onLoop(){ return 1000; }"
                + "public void onPaint(Graphics graphics){ painted = true; }"
                + "public boolean wasPainted(){ return painted; }"
                + "public int boxedIds(){ return GameObjects.all(42).size(); }"
                + "public int filtered(){ return GameObjects.all((GameObject value) -> true).size(); } }"
        );

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "Tests require a JDK, not a JRE");
        assertEquals(0, compiler.run(null, null, null,
            "--release", "11", "-classpath", reference.toString(),
            "-d", classes.toString(), scriptSource.toString()));

        Path jar = scripts.resolve("reference-compiled-script.jar");
        addToJar(jar, classes, "legacy/reference/ReferenceCompiledScript.class");
        ScriptManager manager = ScriptManager.getScriptManager();
        manager.initialize(scripts);
        ScriptDescriptor descriptor = manager.getDiscoveredScripts().stream()
            .filter(candidate -> candidate.getManifest().name().equals("Reference compiled"))
            .findFirst().orElseThrow(() -> new AssertionError(manager.getDiscoveryErrors()));
        assertTrue(descriptor.isLegacy());
        Object script = descriptor.getScriptClass().getDeclaredConstructor().newInstance();
        assertEquals(0, descriptor.getScriptClass().getMethod("boxedIds").invoke(script));
        assertEquals(0, descriptor.getScriptClass().getMethod("filtered").invoke(script));

        AbstractScript running = manager.start(descriptor.getScriptClass());
        for (int attempt = 0; attempt < 100 && manager.getState() != ScriptManager.State.RUNNING;
             attempt++)
            Thread.sleep(10L);
        assertEquals(ScriptManager.State.RUNNING, manager.getState());
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try { manager.paint(graphics); }
        finally { graphics.dispose(); }
        assertEquals(Boolean.TRUE,
            descriptor.getScriptClass().getMethod("wasPainted").invoke(running));
        assertTrue(manager.stop());
    }

    private static Path java(Path root, String relative, String source) throws IOException
    {
        Path path = root.resolve(relative);
        Files.createDirectories(path.getParent());
        Files.write(path, source.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return path;
    }

    private static void addToJar(Path jar, Path classes, String relative) throws IOException
    {
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar)))
        {
            output.putNextEntry(new JarEntry(relative));
            Files.copy(classes.resolve(relative), output);
            output.closeEntry();
        }
    }
}
