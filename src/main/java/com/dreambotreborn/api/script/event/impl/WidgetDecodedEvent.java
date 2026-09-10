package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.WidgetEventListener;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public class WidgetDecodedEvent extends ScriptEvent
{
    private final WidgetChild widget;
    public WidgetDecodedEvent(WidgetChild widget) { this.widget = widget; }
    @Override public void dispatch(EventListener listener)
    {
        if (listener instanceof WidgetEventListener && widget != null)
        {
            ((WidgetEventListener) listener).onWidgetDecoded(widget);
            ((WidgetEventListener) listener).onWidgetLoaded(widget);
        }
    }
}
