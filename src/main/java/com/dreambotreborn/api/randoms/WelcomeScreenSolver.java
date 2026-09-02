package com.dreambotreborn.api.randoms;

import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.wrappers.widgets.Widget;

/** Handles post-login welcome/play widgets by visible text or action. */
public class WelcomeScreenSolver extends RandomSolver
{
    public WelcomeScreenSolver()
    {
        super(RandomEvent.WELCOME_SCREEN);
    }

    @Override
    public boolean shouldExecute()
    {
        return welcomeWidget() != null;
    }

    @Override
    public int onLoop()
    {
        Widget widget = welcomeWidget();
        if (widget != null)
        {
            if (!widget.actions.isEmpty()) widget.interact(widget.actions.get(0));
            else widget.click();
        }
        return 800;
    }

    private static Widget welcomeWidget()
    {
        return Widgets.visible().filter(widget ->
        {
            String text = (widget.text + ' ' + widget.name).toLowerCase();
            return text.contains("click here to play") || text.equals("play")
                || text.contains("welcome to runescape");
        }).first();
    }
}
