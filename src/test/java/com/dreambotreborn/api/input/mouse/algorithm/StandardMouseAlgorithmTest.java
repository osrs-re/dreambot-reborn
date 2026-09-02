package com.dreambotreborn.api.input.mouse.algorithm;

import java.awt.Point;
import com.dreambotreborn.api.methods.input.mouse.MouseSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StandardMouseAlgorithmTest
{
    @AfterEach
    void resetProfile()
    {
        MouseProfile.resetToDefaults();
    }

    @Test
    void convergesWithoutTeleportingOrStalling()
    {
        MouseProfile.setSpeed(900.0);
        StandardMouseAlgorithm algorithm = new StandardMouseAlgorithm();
        Point current = new Point(10, 10);
        Point target = new Point(700, 450);
        algorithm.begin(current, target, new MouseSettings().setOvershoot(false));
        int movements = 0;
        for (int step = 0; step < 2_000 && !algorithm.isComplete(current, target); step++)
        {
            Point next = algorithm.next(current, target, 1.0 / 60.0);
            if (!next.equals(current)) movements++;
            current = next;
        }
        assertTrue(current.distance(target) <= 2.0, "mouse should converge on its destination");
        assertTrue(movements > 10, "movement should contain a visible trajectory");
    }
}
