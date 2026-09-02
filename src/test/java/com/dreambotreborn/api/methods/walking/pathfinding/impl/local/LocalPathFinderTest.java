package com.dreambotreborn.api.methods.walking.pathfinding.impl.local;

import java.util.List;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.pathfinding.data.TileFlags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalPathFinderTest
{
    @Test
    void routesAroundCollisionAndThroughGap()
    {
        int[][] flags = new int[7][7];
        for (int y = 0; y < 7; y++) if (y != 5) flags[3][y] = TileFlags.BLOCKED;
        LocalPathFinder finder = new LocalPathFinder();
        List<Tile> path = finder.find(flags, 3200, 3200, 0,
            new Tile(3201, 3201, 0), new Tile(3205, 3201, 0));

        assertFalse(path.isEmpty());
        assertEquals(new Tile(3201, 3201, 0), path.get(0));
        assertEquals(new Tile(3205, 3201, 0), path.get(path.size() - 1));
        assertTrue(path.contains(new Tile(3203, 3205, 0)));
    }

    @Test
    void doesNotCutDiagonallyAcrossBlockedOrthogonalTile()
    {
        int[][] flags = new int[3][3];
        flags[1][0] = TileFlags.BLOCKED;
        LocalPathFinder finder = new LocalPathFinder();
        assertFalse(finder.canMove(flags, 0, 0, 1, 1));
    }
}
