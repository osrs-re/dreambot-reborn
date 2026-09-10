package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.graphics.Projectile;
import net.runelite.api.events.ProjectileMoved;

public interface ProjectileListener extends EventListener
{
    default void onTargeted(Projectile projectile, Tile tile) { }
    default void preTargeted(Projectile projectile) { }
    default void postTargeted(Projectile projectile) { }

    /** RuneLite-native callback retained for lower-level integrations. */
    default void onProjectileMoved(ProjectileMoved event) { }
}
