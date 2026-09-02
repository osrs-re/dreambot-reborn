package com.dreambotreborn.api.script.listener;

import java.util.EventListener;
import net.runelite.api.events.ChatMessage;

public interface ChatListener extends EventListener
{
    default void onMessage(ChatMessage message) { }
}
