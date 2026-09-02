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
public final class GameObject extends Entity
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

    private final TileObject runeLiteObject;

    public GameObject(TileObject runeLiteObject, ObjectComposition composition, Type type)
    {
        super(
            runeLiteObject.getId(),
            composition == null ? null : composition.getName(),
            composition == null ? null : composition.getActions(),
            runeLiteObject.getWorldLocation(),
            runeLiteObject.getLocalLocation());
        this.runeLiteObject = runeLiteObject;
        this.type = type;
        this.plane = runeLiteObject.getPlane();
    }

    public Type getType()
    {
        return type;
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
