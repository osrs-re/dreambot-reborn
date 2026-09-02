package com.dreambotreborn.api.input.mouse.algorithm;

import com.dreambotreborn.api.input.event.impl.mouse.MouseButton;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;

/** DreamBot-compatible high-level mouse algorithm contract. */
public interface MouseAlgorithm
{
    boolean handleMovement(AbstractMouseDestination<?> destination);

    boolean handleClick(MouseButton button);
}
