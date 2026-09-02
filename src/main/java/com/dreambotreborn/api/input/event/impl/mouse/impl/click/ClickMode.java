package com.dreambotreborn.api.input.event.impl.mouse.impl.click;

import com.dreambotreborn.api.input.event.impl.mouse.MouseButton;

/** Compatibility click modes. */
public enum ClickMode
{
    NULL(MouseButton.NULL),
    LEFT_CLICK(MouseButton.LEFT_CLICK),
    RIGHT_CLICK(MouseButton.RIGHT_CLICK),
    MIDDLE_CLICK(MouseButton.MIDDLE_CLICK);

    private final MouseButton button;

    ClickMode(MouseButton button)
    {
        this.button = button;
    }

    public int getId()
    {
        return button.getId();
    }

    public int getMask()
    {
        return button.getMask();
    }

    public MouseButton getButton()
    {
        return button;
    }
}
