package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import com.dreambotreborn.api.wrappers.widgets.message.Message;

public interface ChatListener extends EventListener
{
    default void onAutoMessage(Message message) { }
    default void onPrivateInfoMessage(Message message) { }
    default void onClanMessage(Message message) { }
    default void onMessage(Message message) { }
    default void onGameMessage(Message message) { }
    default void onPlayerMessage(Message message) { }
    default void onTradeMessage(Message message) { }
    default void onPrivateInMessage(Message message) { }
    default void onPrivateOutMessage(Message message) { }

    /** RuneLite-native callback retained for lower-level integrations. */
    default void onMessage(net.runelite.api.events.ChatMessage message) { }
}
