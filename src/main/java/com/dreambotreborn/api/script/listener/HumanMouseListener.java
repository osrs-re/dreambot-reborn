package com.dreambotreborn.api.script.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EventListener;

public interface HumanMouseListener extends EventListener
{
    default void onMouseClicked(MouseEvent event) { }
    default void onMousePressed(MouseEvent event) { }
    default void onMouseReleased(MouseEvent event) { }
    default void onMouseEntered(MouseEvent event) { }
    default void onMouseExited(MouseEvent event) { }
    default void onMouseDragged(MouseEvent event) { }
    default void onMouseMoved(MouseEvent event) { }
    default void onMouseWheelMoved(MouseWheelEvent event) { }
}
