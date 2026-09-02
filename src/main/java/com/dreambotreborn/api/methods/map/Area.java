package com.dreambotreborn.api.methods.map;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.dreambotreborn.api.wrappers.interactive.Entity;
import com.dreambotreborn.api.wrappers.interactive.Locatable;

/** Polygonal world area with DreamBot-style containment and random-tile helpers. */
public class Area implements Locatable
{
    private final Polygon polygon;
    private int z;

    public Area(Tile... tiles)
    {
        if (tiles == null || tiles.length < 3)
        {
            throw new IllegalArgumentException("An area needs at least three boundary tiles");
        }
        polygon = new Polygon();
        for (Tile tile : tiles)
        {
            if (tile == null)
            {
                throw new IllegalArgumentException("Boundary tiles cannot be null");
            }
            polygon.addPoint(tile.getX(), tile.getY());
        }
        z = tiles[0].getZ();
    }

    public Area(Tile southWest, Tile northEast)
    {
        this(southWest.getX(), southWest.getY(), northEast.getX(), northEast.getY(), southWest.getZ());
    }

    public Area(int x1, int y1, int x2, int y2)
    {
        this(x1, y1, x2, y2, 0);
    }

    public Area(int x1, int y1, int x2, int y2, int z)
    {
        int minimumX = Math.min(x1, x2);
        int maximumX = Math.max(x1, x2);
        int minimumY = Math.min(y1, y2);
        int maximumY = Math.max(y1, y2);
        polygon = new Polygon(
            new int[] {minimumX, maximumX, maximumX, minimumX},
            new int[] {minimumY, minimumY, maximumY, maximumY}, 4);
        this.z = z;
    }

    public static Area generateArea(int radius, Tile center)
    {
        return center.getArea(Math.max(0, radius));
    }

    public void setZ(int z) { this.z = z; }

    public Tile[] getTiles()
    {
        Rectangle bounds = polygon.getBounds();
        List<Tile> tiles = new ArrayList<>();
        for (int x = bounds.x; x <= bounds.x + bounds.width; x++)
        {
            for (int y = bounds.y; y <= bounds.y + bounds.height; y++)
            {
                if (contains(x, y)) tiles.add(new Tile(x, y, z));
            }
        }
        return tiles.toArray(new Tile[0]);
    }

    public Tile getNearestTile(Entity entity)
    {
        Tile origin = entity == null ? null : entity.getTile();
        Tile nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (Tile tile : getTiles())
        {
            double distance = origin == null ? tile.distance() : tile.distance(origin);
            if (distance < nearestDistance)
            {
                nearest = tile;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    public Tile getRandomTile()
    {
        Tile[] tiles = getTiles();
        return tiles.length == 0 ? null : tiles[ThreadLocalRandom.current().nextInt(tiles.length)];
    }

    public Tile getCenter()
    {
        Rectangle bounds = polygon.getBounds();
        return new Tile((int) Math.round(bounds.getCenterX()), (int) Math.round(bounds.getCenterY()), z);
    }

    public Polygon getPolygonArea()
    {
        return new Polygon(polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    public Rectangle getBoundingBox()
    {
        return polygon.getBounds();
    }

    public boolean contains(int x, int y)
    {
        return polygon.contains(x + 0.5, y + 0.5) || polygon.contains(x, y);
    }

    public boolean contains(Tile tile)
    {
        return tile != null && tile.getZ() == z && contains(tile.getX(), tile.getY());
    }

    public boolean contains(Entity entity)
    {
        return entity != null && contains(entity.getTile());
    }

    public Area withArea(Area other)
    {
        if (other == null) return this;
        Rectangle combined = getBoundingBox().union(other.getBoundingBox());
        return new Area(combined.x, combined.y,
            combined.x + combined.width, combined.y + combined.height, z);
    }

    public List<Tile> getBoundaryPoints()
    {
        List<Tile> result = new ArrayList<>();
        for (int index = 0; index < polygon.npoints; index++)
        {
            result.add(new Tile(polygon.xpoints[index], polygon.ypoints[index], z));
        }
        return result;
    }

    @Override
    public Tile getTile()
    {
        return getCenter();
    }
}
