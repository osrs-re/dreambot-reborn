package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.graphics.SpotAnimation;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;
import net.runelite.api.events.AnimationChanged;

public interface AnimationListener extends EventListener
{
    default void onPlayerAnimation(Player player, int previous, int current) { }
    default void onNpcAnimation(NPC npc, int previous, int current) { }
    default void onPlayerSpotAnimation(Player player, SpotAnimation animation) { }
    default void onNPCSpotAnimation(NPC npc, SpotAnimation animation) { }

    /** RuneLite-native callback retained for scripts that need the original event. */
    default void onAnimationChanged(AnimationChanged event) { }
}
