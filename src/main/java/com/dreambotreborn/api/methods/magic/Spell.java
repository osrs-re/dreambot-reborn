package com.dreambotreborn.api.methods.magic;

import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.magic.cost.Rune;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public interface Spell
{
    int getLevel();
    int getParent();
    int getChild();
    Rune[] getCost();
    int getMaxHit();

    default WidgetChild getWidget()
    {
        net.runelite.api.widgets.Widget widget = DreamBotRebornApi.requireClient().getWidget(getParent(), getChild());
        return widget == null ? null : new WidgetChild(widget);
    }
}
