package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;

public interface ActionListener extends EventListener
{
    default void onAction(MenuRow row, int mouseX, int mouseY) { }
}
