package com.dreambotreborn.api.script;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.dreambotreborn.api.script.frameworks.treebranch.Branch;
import com.dreambotreborn.api.script.frameworks.treebranch.Leaf;
import com.dreambotreborn.api.script.frameworks.treebranch.TreeScript;
import com.dreambotreborn.api.script.impl.TaskScript;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptFrameworkTest
{
    @AfterEach
    void stopManager()
    {
        ScriptManager.getScriptManager().shutdown();
    }

    @Test
    void taskScriptUsesHighestAcceptedPriorityAndDreamBotFailLimitSemantics()
    {
        TestTaskScript script = new TestTaskScript();
        script.addNodes(node(2, true, 200), node(10, true, 1_000), node(20, false, 2_000));

        assertEquals(1_000, script.onLoop());
        assertEquals(10, script.getLastTaskNode().priority());

        TestTaskScript empty = new TestTaskScript();
        empty.setFailLimit(1);
        assertEquals(1_000, empty.onLoop());
        assertEquals(1_000, empty.onLoop());
        assertEquals(1_000, empty.onLoop());
        assertEquals(-1, empty.onLoop());
    }

    @Test
    void treeScriptRunsFirstValidLeafAndTracksNames()
    {
        TestTreeScript tree = new TestTreeScript();
        ValidBranch branch = new ValidBranch();
        branch.addLeaves(new InvalidLeaf(), new ValidLeaf());
        tree.addBranches(branch);

        assertEquals(321, tree.onLoop());
        assertEquals("ValidBranch", tree.getCurrentBranchName());
        assertEquals("ValidLeaf", tree.getCurrentLeafName());
        assertEquals(tree, branch.getTree());
        assertEquals(branch, branch.getChildren().get(1).getParent());
    }

    @Test
    void managerDiscoversRunsPaintsPausesAndStops(@TempDir Path directory) throws Exception
    {
        ScriptManager manager = ScriptManager.getScriptManager();
        manager.initialize(directory.resolve("scripts"));
        assertTrue(manager.getDiscoveredScripts().stream()
            .anyMatch(script -> script.getManifest().name().equals("Runtime Paint Demo")));

        LifecycleScript.reset();
        AbstractScript script = manager.start(LifecycleScript.class);
        assertTrue(LifecycleScript.looped.await(2, TimeUnit.SECONDS));
        assertTrue(manager.isRunning());
        assertNotNull(script.getManifest());

        BufferedImage image = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        manager.paint(graphics);
        graphics.dispose();
        assertEquals(1, LifecycleScript.paints.get());

        assertTrue(manager.pause());
        assertTrue(manager.isPaused());
        assertEquals(1, LifecycleScript.pauses.get());
        assertTrue(manager.resume());
        assertFalse(manager.isPaused());
        assertEquals(1, LifecycleScript.resumes.get());

        assertTrue(manager.stop());
        assertTrue(LifecycleScript.exited.await(2, TimeUnit.SECONDS));
        assertEquals(ScriptManager.State.STOP, manager.getState());
    }

    private static TaskNode node(int priority, boolean accepted, int result)
    {
        return new TaskNode()
        {
            @Override
            public int priority()
            {
                return priority;
            }

            @Override
            public boolean accept()
            {
                return accepted;
            }

            @Override
            public int execute()
            {
                return result;
            }
        };
    }

    private static final class TestTaskScript extends TaskScript
    {
    }

    private static final class TestTreeScript extends TreeScript
    {
    }

    private static final class ValidBranch extends Branch
    {
        @Override
        public boolean isValid()
        {
            return true;
        }
    }

    private static final class InvalidLeaf extends Leaf
    {
        @Override
        public boolean isValid()
        {
            return false;
        }

        @Override
        public int onLoop()
        {
            return 999;
        }
    }

    private static final class ValidLeaf extends Leaf
    {
        @Override
        public boolean isValid()
        {
            return true;
        }

        @Override
        public int onLoop()
        {
            return 321;
        }
    }

    @ScriptManifest(
        name = "Lifecycle Test",
        author = "Tests",
        category = Category.UTILITY,
        version = 1.0)
    public static final class LifecycleScript extends AbstractScript
    {
        private static CountDownLatch looped;
        private static CountDownLatch exited;
        private static AtomicInteger paints;
        private static AtomicInteger pauses;
        private static AtomicInteger resumes;

        static void reset()
        {
            looped = new CountDownLatch(1);
            exited = new CountDownLatch(1);
            paints = new AtomicInteger();
            pauses = new AtomicInteger();
            resumes = new AtomicInteger();
        }

        @Override
        public int onLoop()
        {
            looped.countDown();
            return 1_000;
        }

        @Override
        public void onPause()
        {
            pauses.incrementAndGet();
        }

        @Override
        public void onResume()
        {
            resumes.incrementAndGet();
        }

        @Override
        public void onPaint(Graphics2D graphics)
        {
            paints.incrementAndGet();
        }

        @Override
        public void onExit()
        {
            exited.countDown();
        }
    }
}
