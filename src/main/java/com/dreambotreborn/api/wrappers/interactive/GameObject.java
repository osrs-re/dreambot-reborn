package com.dreambotreborn.api.wrappers.interactive;

import java.awt.Component;
import java.awt.Shape;
import net.runelite.api.ObjectComposition;
import net.runelite.api.MenuAction;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import com.dreambotreborn.api.DreamBotRebornApi;

/**
 * A stable snapshot of a RuneLite tile object and its object definition.
 *
 * <p>The public fields intentionally support compact query expressions such as
 * {@code object -> object.name.equals("Bank booth")}.</p>
 */
public class GameObject extends Entity
{
    public enum Type
    {
        GAME,
        WALL,
        DECORATIVE,
        GROUND
    }

    public final Type type;
    public final int plane;

    protected final TileObject runeLiteObject;
    protected final ObjectComposition composition;

    public GameObject(TileObject runeLiteObject, ObjectComposition composition, Type type)
    {
        super(
            runeLiteObject.getId(),
            composition == null ? null : composition.getName(),
            composition == null ? null : composition.getActions(),
            runeLiteObject.getWorldLocation(),
            runeLiteObject.getLocalLocation());
        this.runeLiteObject = runeLiteObject;
        this.composition = composition;
        this.type = type;
        this.plane = runeLiteObject.getPlane();
    }

    public Type getObjectType()
    {
        return type;
    }

    public int getOrientation()
    {
        if (runeLiteObject instanceof net.runelite.api.GameObject)
            return ((net.runelite.api.GameObject) runeLiteObject).getOrientation();
        if (runeLiteObject instanceof net.runelite.api.WallObject)
            return ((net.runelite.api.WallObject) runeLiteObject).getOrientationA();
        return 0;
    }

    public int getCurrentOrientation() { return getOrientation(); }
    public int getFlags()
    {
        if (runeLiteObject instanceof net.runelite.api.GameObject)
            return ((net.runelite.api.GameObject) runeLiteObject).getConfig();
        if (runeLiteObject instanceof net.runelite.api.WallObject)
            return ((net.runelite.api.WallObject) runeLiteObject).getConfig();
        if (runeLiteObject instanceof net.runelite.api.DecorativeObject)
            return ((net.runelite.api.DecorativeObject) runeLiteObject).getConfig();
        if (runeLiteObject instanceof net.runelite.api.GroundObject)
            return ((net.runelite.api.GroundObject) runeLiteObject).getConfig();
        return 0;
    }
    public long getIndex() { return runeLiteObject.getHash(); }
    public int getGridX() { return localLocation == null ? -1 : localLocation.getSceneX(); }
    public int getGridY() { return localLocation == null ? -1 : localLocation.getSceneY(); }
    public int getLocalX() { return localLocation == null ? -1 : localLocation.getX(); }
    public int getLocalY() { return localLocation == null ? -1 : localLocation.getY(); }
    public int getRealID() { return id; }
    public int getHeight() { return composition == null ? 1 : composition.getSizeY(); }
    public int getWidth() { return composition == null ? 1 : composition.getSizeX(); }
    public int[] getAlternativeIDs()
    {
        int[] ids = composition == null ? null : composition.getImpostorIds();
        return ids == null ? new int[0] : java.util.Arrays.copyOf(ids, ids.length);
    }
    public int getVarpID() { return composition == null ? -1 : composition.getVarPlayerId(); }
    public int getVarbitID() { return composition == null ? -1 : composition.getVarbitId(); }
    public boolean hasChildDefinitions() { return getAlternativeIDs().length > 0; }
    public int getMapSceneID() { return composition == null ? -1 : composition.getMapSceneId(); }
    public int getMiniMapIcon() { return composition == null ? -1 : composition.getMapIconId(); }
    public java.util.List<com.dreambotreborn.api.methods.map.Tile> getObjectTiles()
    {
        java.util.List<com.dreambotreborn.api.methods.map.Tile> result = new java.util.ArrayList<>();
        com.dreambotreborn.api.methods.map.Tile origin = getTile();
        if (origin == null) return result;
        for (int x = 0; x < getWidth(); x++)
            for (int y = 0; y < getHeight(); y++) result.add(origin.translate(x, y));
        return java.util.Collections.unmodifiableList(result);
    }
    public java.util.List<com.dreambotreborn.api.methods.map.Tile> getSurrounding()
    {
        java.util.LinkedHashSet<com.dreambotreborn.api.methods.map.Tile> result =
            new java.util.LinkedHashSet<>();
        for (com.dreambotreborn.api.methods.map.Tile tile : getObjectTiles())
            for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++)
                if (x != 0 || y != 0) result.add(tile.translate(x, y));
        result.removeAll(getObjectTiles());
        return java.util.Collections.unmodifiableList(new java.util.ArrayList<>(result));
    }
    public java.util.List<com.dreambotreborn.api.methods.map.Tile> getInteractableFrom()
    {
        java.util.List<com.dreambotreborn.api.methods.map.Tile> result = new java.util.ArrayList<>();
        for (com.dreambotreborn.api.methods.map.Tile tile : getSurrounding())
            if (com.dreambotreborn.api.methods.walking.impl.Walking.canReach(tile)) result.add(tile);
        return java.util.Collections.unmodifiableList(result);
    }
    public boolean canReach(com.dreambotreborn.api.methods.map.Tile from)
    {
        if (from == null) return canReach();
        for (com.dreambotreborn.api.methods.map.Tile tile : getSurrounding())
            if (com.dreambotreborn.api.methods.map.Map.canReach(from, tile)) return true;
        return false;
    }

    protected static ObjectComposition definition(TileObject object)
    {
        if (object == null) throw new IllegalArgumentException("reference must be a RuneLite TileObject");
        ObjectComposition value = DreamBotRebornApi.requireClient().getObjectDefinition(object.getId());
        if (value != null && value.getImpostorIds() != null && value.getImpostor() != null)
            value = value.getImpostor();
        return value;
    }

    /**
     * Returns the underlying RuneLite object. Its live methods should only be
     * called from the game thread.
     */
    public TileObject unwrap()
    {
        return runeLiteObject;
    }

    @Override
    public Shape clickShape(Component component)
    {
        return runeLiteObject.getClickbox();
    }

    @Override
    public InteractionSpec interactionAt(int index)
    {
        if (index < 0 || index >= 5 || localLocation == null)
        {
            return null;
        }

        int param0 = localLocation.getSceneX();
        int param1 = localLocation.getSceneY();
        if (runeLiteObject instanceof net.runelite.api.GameObject)
        {
            net.runelite.api.Point sceneMinimum =
                ((net.runelite.api.GameObject) runeLiteObject).getSceneMinLocation();
            if (sceneMinimum != null)
            {
                param0 = sceneMinimum.getX();
                param1 = sceneMinimum.getY();
            }
        }

        MenuAction[] actions =
        {
            MenuAction.GAME_OBJECT_FIRST_OPTION,
            MenuAction.GAME_OBJECT_SECOND_OPTION,
            MenuAction.GAME_OBJECT_THIRD_OPTION,
            MenuAction.GAME_OBJECT_FOURTH_OPTION,
            MenuAction.GAME_OBJECT_FIFTH_OPTION
        };
        WorldView worldView = runeLiteObject.getWorldView();
        return new InteractionSpec(
            param0,
            param1,
            DreamBotRebornApi.requireClient().isWidgetSelected()
                ? MenuAction.WIDGET_TARGET_ON_GAME_OBJECT : actions[index],
            id,
            actionAt(index),
            name,
            worldView == null ? WorldView.TOPLEVEL : worldView.getId());
    }

    @Override
    public String toString()
    {
        return "GameObject{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", type=" + type +
            ", worldLocation=" + worldLocation +
            '}';
    }
}
