package com.dreambotreborn.scripts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.atomic.AtomicLong;
import com.dreambotreborn.api.script.Category;
import com.dreambotreborn.api.script.ScriptManifest;
import com.dreambotreborn.api.script.TaskNode;
import com.dreambotreborn.api.script.impl.TaskScript;

/** Built-in example that demonstrates TaskScript and onPaint. */
@ScriptManifest(
    name = "Runtime Paint Demo",
    author = "DreamBot Reborn",
    description = "Demonstrates ScriptManifest, TaskScript lifecycle and onPaint runtime information.",
    category = Category.UTILITY,
    version = 1.0)
public final class RuntimePaintDemo extends TaskScript
{
    private final AtomicLong loops = new AtomicLong();

    @Override
    public void onStart()
    {
        addNodes(new TaskNode()
        {
            @Override
            public boolean accept()
            {
                return true;
            }

            @Override
            public int execute()
            {
                loops.incrementAndGet();
                return 500;
            }
        });
    }

    @Override
    public void onPaint(Graphics2D graphics)
    {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(18, 20, 24, 205));
        graphics.fillRoundRect(12, 12, 224, 84, 12, 12);
        graphics.setColor(new Color(70, 173, 214));
        graphics.fillRoundRect(12, 12, 5, 84, 10, 10);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        graphics.setColor(Color.WHITE);
        graphics.drawString(getManifest().name(), 28, 36);
        graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        graphics.setColor(new Color(210, 214, 220));
        graphics.drawString("Runtime: " + getScriptTimer().formatTime(), 28, 60);
        graphics.drawString("Loops:   " + loops.get(), 28, 80);
    }
}
