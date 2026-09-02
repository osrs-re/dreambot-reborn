package com.dreambotreborn.api.internal;

import java.awt.Component;
import java.awt.Shape;
import net.runelite.api.MenuAction;

/** Internal description used to route an API interaction through VirtualMouse. */
public interface MouseTarget
{
    int firstActionIndex();

    int actionIndex(String action);

    String actionAt(int index);

    Shape clickShape(Component component);

    InteractionSpec interactionAt(int index);

    final class InteractionSpec
    {
        public final int param0;
        public final int param1;
        public final MenuAction action;
        public final int identifier;
        public final String option;
        public final String target;
        public final int worldViewId;

        public InteractionSpec(
            int param0,
            int param1,
            MenuAction action,
            int identifier,
            String option,
            String target,
            int worldViewId)
        {
            this.param0 = param0;
            this.param1 = param1;
            this.action = action;
            this.identifier = identifier;
            this.option = option;
            this.target = target;
            this.worldViewId = worldViewId;
        }
    }
}
