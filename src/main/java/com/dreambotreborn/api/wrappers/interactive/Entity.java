package com.dreambotreborn.api.wrappers.interactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.awt.Rectangle;
import java.awt.Shape;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.internal.MouseTarget;

/** Immutable common snapshot for a locatable game entity. */
public abstract class Entity
    extends com.dreambotreborn.api.wrappers.interactive.interact.Interactable
    implements MouseTarget, Locatable, Identifiable, Verified
{
    public final int id;
    public final String name;
    public final List<String> actions;
    public final WorldPoint worldLocation;
    public final LocalPoint localLocation;

    private final String[] actionSlots;

    protected Entity(int id, String name, String[] actions, WorldPoint worldLocation, LocalPoint localLocation)
    {
        this.id = id;
        this.name = clean(name);
        this.actionSlots = copySlots(actions);
        this.actions = compact(actionSlots);
        this.worldLocation = worldLocation;
        this.localLocation = localLocation;
    }

    @Override
    public final int getId()
    {
        return id;
    }

    @Override
    public final String getName()
    {
        return name;
    }

    @Override
    public final List<String> getActions()
    {
        return actions;
    }

    public final WorldPoint getWorldLocation()
    {
        return worldLocation;
    }

    public final LocalPoint getLocalLocation()
    {
        return localLocation;
    }

    @Override
    public final com.dreambotreborn.api.methods.map.Tile getTile()
    {
        return worldLocation == null ? null : new com.dreambotreborn.api.methods.map.Tile(worldLocation);
    }

    public final int getPlane()
    {
        return worldLocation == null ? -1 : worldLocation.getPlane();
    }

    public final int getX()
    {
        return worldLocation == null ? -1 : worldLocation.getX();
    }

    public final int getY()
    {
        return worldLocation == null ? -1 : worldLocation.getY();
    }

    public final com.dreambotreborn.api.methods.map.Tile getServerTile()
    {
        return getTile();
    }

    public boolean isOnScreen()
    {
        Shape shape = clickShape(null);
        return shape != null && shape.getBounds().intersects(0, 0,
            DreamBotRebornApi.requireClient().getCanvasWidth(),
            DreamBotRebornApi.requireClient().getCanvasHeight());
    }

    public Rectangle getBoundingBox()
    {
        Shape shape = clickShape(null);
        return shape == null ? new Rectangle() : shape.getBounds();
    }

    /** Returns the live RuneLite object represented by this snapshot. */
    public Object getReference()
    {
        if (this instanceof GameObject) return ((GameObject) this).unwrap();
        if (this instanceof NPC) return ((NPC) this).unwrap();
        if (this instanceof Player) return ((Player) this).unwrap();
        return null;
    }

    public int getRenderableHeight()
    {
        Object reference = getReference();
        return reference instanceof net.runelite.api.Renderable
            ? ((net.runelite.api.Renderable) reference).getModelHeight() : 0;
    }

    /** Chebyshev tile distance from the local player, or infinity when unavailable. */
    public final int distance()
    {
        net.runelite.api.Player local = DreamBotRebornApi.requireClient().getLocalPlayer();
        WorldPoint origin = local == null ? null : local.getWorldLocation();
        return distance(origin);
    }

    public final int distance(WorldPoint origin)
    {
        if (origin == null || worldLocation == null || origin.getPlane() != worldLocation.getPlane())
        {
            return Integer.MAX_VALUE;
        }
        return origin.distanceTo2D(worldLocation);
    }

    public final double distance(com.dreambotreborn.api.methods.map.Tile tile)
    {
        return tile == null ? Double.POSITIVE_INFINITY : tile.distance(getTile());
    }

    public final double distance(Entity entity)
    {
        return entity == null ? Double.POSITIVE_INFINITY : distance(entity.getTile());
    }

    public final double tileDistance(com.dreambotreborn.api.methods.map.Tile tile)
    {
        return distance(tile);
    }

    public final double tileDistance(Entity entity)
    {
        return distance(entity);
    }

    public final double walkingDistance(com.dreambotreborn.api.methods.map.Tile tile)
    {
        return tile == null ? Double.POSITIVE_INFINITY : tile.walkingDistance(getTile());
    }

    @Override
    public final boolean hasAction(String action)
    {
        return action != null && actionIndex(action) >= 0;
    }

    public final boolean hasAction(String... requestedActions)
    {
        if (requestedActions != null)
        {
            for (String action : requestedActions)
            {
                if (hasAction(action)) return true;
            }
        }
        return false;
    }

    public final String getLeftClickAction()
    {
        return actionAt(firstActionIndex());
    }

    public boolean exists()
    {
        if (this instanceof GameObject)
        {
            return com.dreambotreborn.api.methods.interactive.GameObjects.all().stream()
                .anyMatch(value -> value.unwrap() == getReference());
        }
        if (this instanceof NPC)
        {
            return com.dreambotreborn.api.methods.interactive.NPCs.all().stream()
                .anyMatch(value -> value.unwrap() == getReference());
        }
        if (this instanceof Player)
        {
            return com.dreambotreborn.api.methods.interactive.Players.all().stream()
                .anyMatch(value -> value.unwrap() == getReference());
        }
        return false;
    }

    public com.dreambotreborn.api.methods.map.Area getSurroundingArea(int radius)
    {
        com.dreambotreborn.api.methods.map.Tile tile = getTile();
        return tile == null ? null : tile.getArea(Math.max(0, radius));
    }

    public boolean canReach()
    {
        return com.dreambotreborn.api.methods.walking.impl.Walking.canReach(getTile());
    }

    @Override
    public final int firstActionIndex()
    {
        for (int i = 0; i < actionSlots.length; i++)
        {
            if (actionSlots[i] != null)
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public final int actionIndex(String action)
    {
        if (action == null)
        {
            return -1;
        }
        for (int i = 0; i < actionSlots.length; i++)
        {
            if (actionSlots[i] != null && actionSlots[i].equalsIgnoreCase(action))
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    public final String actionAt(int index)
    {
        return index < 0 || index >= actionSlots.length ? null : actionSlots[index];
    }

    public static String clean(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]*>", "");
    }

    public static String[] copySlots(String[] source)
    {
        String[] slots = source == null ? new String[5] : Arrays.copyOf(source, Math.max(5, source.length));
        for (int i = 0; i < slots.length; i++)
        {
            if (slots[i] != null)
            {
                slots[i] = clean(slots[i]);
            }
        }
        return slots;
    }

    public static List<String> compact(String[] slots)
    {
        List<String> result = new ArrayList<>();
        for (String action : slots)
        {
            if (action != null && !action.isEmpty())
            {
                result.add(action);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
