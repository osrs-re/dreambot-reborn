package com.dreambotreborn.api.methods.walking.pathfinding.impl.web;

import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.path.impl.GlobalPath;
import com.dreambotreborn.api.methods.walking.web.node.AbstractWebNode;
import com.dreambotreborn.api.methods.walking.web.node.impl.BasicWebNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebFinderTest
{
    private final WebFinder finder = WebFinder.getWebFinder();

    @AfterEach
    void clear() { finder.clearCustomNodes(); }

    @Test
    void routesAcrossConnectedCustomNodes()
    {
        finder.clearCustomNodes();
        BasicWebNode first = new BasicWebNode(3200, 3200, 0);
        BasicWebNode middle = new BasicWebNode(3210, 3200, 0);
        BasicWebNode last = new BasicWebNode(3220, 3200, 0);
        first.addDualConnections(middle);
        middle.addDualConnections(last);
        finder.addWebNodes(first, middle, last);

        GlobalPath<AbstractWebNode> path = finder.calculate(
            new Tile(3200, 3200, 0), new Tile(3220, 3200, 0));

        assertFalse(path.isEmpty());
        assertTrue(path.contains(middle));
        assertEquals(new Tile(3220, 3200, 0), path.getDestination());
    }

    @Test
    void createsMinimapSizedWaypointsWithoutAWebDataset()
    {
        finder.clearCustomNodes();
        GlobalPath<AbstractWebNode> path = finder.calculate(
            new Tile(3000, 3000, 0), new Tile(3050, 3030, 0));
        assertTrue(path.size() >= 6);
        for (int index = 1; index < path.size(); index++)
            assertTrue(path.get(index - 1).distance(path.get(index).getTile()) <= 10);
    }
}
