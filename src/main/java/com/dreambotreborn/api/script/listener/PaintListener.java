package com.dreambotreborn.api.script.listener;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EventListener;

/** Receives the game buffer after RuneScape has rendered its frame. */
public interface PaintListener extends EventListener
{
    default void onPaint(Graphics graphics)
    {
        if (graphics instanceof Graphics2D)
        {
            onPaint((Graphics2D) graphics);
        }
    }

    default void onPaint(Graphics2D graphics)
    {
    }
}
