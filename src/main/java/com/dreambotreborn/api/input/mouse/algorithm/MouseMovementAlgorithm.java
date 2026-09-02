package com.dreambotreborn.api.input.mouse.algorithm;

import java.awt.Point;
import com.dreambotreborn.api.input.Mouse;
import com.dreambotreborn.api.input.event.impl.mouse.MouseButton;
import com.dreambotreborn.api.input.mouse.destination.AbstractMouseDestination;
import com.dreambotreborn.api.methods.input.mouse.MouseSettings;

/** Stateful trajectory generator used one step at a time by the game thread. */
public interface MouseMovementAlgorithm extends MouseAlgorithm
{
    void begin(Point start, Point target, MouseSettings settings);

    Point next(Point current, Point target, double elapsedSeconds);

    default boolean isComplete(Point current, Point target)
    {
        return current != null && target != null && current.distance(target) <= 1.5;
    }

    void reset();

    @Override
    default boolean handleMovement(AbstractMouseDestination<?> destination)
    {
        return Mouse.move(destination);
    }

    @Override
    default boolean handleClick(MouseButton button)
    {
        return Mouse.click(button);
    }
}
