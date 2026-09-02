package com.dreambotreborn.api.input.event.impl.mouse;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/** Mouse buttons and their AWT identifiers, matching DreamBot's public naming. */
public enum MouseButton
{
    NULL(MouseEvent.NOBUTTON, 0),
    LEFT_CLICK(MouseEvent.BUTTON1, InputEvent.BUTTON1_DOWN_MASK),
    MIDDLE_CLICK(MouseEvent.BUTTON2, InputEvent.BUTTON2_DOWN_MASK),
    RIGHT_CLICK(MouseEvent.BUTTON3, InputEvent.BUTTON3_DOWN_MASK);

    private static final EnumSet<MouseButton> PRESSED = EnumSet.noneOf(MouseButton.class);

    private final int id;
    private final int mask;

    MouseButton(int id, int mask)
    {
        this.id = id;
        this.mask = mask;
    }

    public int getId()
    {
        return id;
    }

    public int getMask()
    {
        return mask;
    }

    public static synchronized List<MouseButton> getPressed()
    {
        return Collections.unmodifiableList(new ArrayList<>(PRESSED));
    }

    public static MouseButton getForId(int id)
    {
        for (MouseButton button : values())
        {
            if (button.id == id)
            {
                return button;
            }
        }
        return NULL;
    }

    public static synchronized void markPressed(MouseButton button, boolean pressed)
    {
        if (button == null || button == NULL)
        {
            return;
        }
        if (pressed)
        {
            PRESSED.add(button);
        }
        else
        {
            PRESSED.remove(button);
        }
    }

    public static synchronized void reset()
    {
        PRESSED.clear();
    }
}
