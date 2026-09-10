package com.dreambotreborn.api.script.event.impl;

import java.util.EventListener;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.listener.ChatListener;
import com.dreambotreborn.api.wrappers.widgets.message.Message;
import com.dreambotreborn.api.wrappers.widgets.message.MessageType;

public class MessageEvent extends ScriptEvent
{
    private final Message message;
    public MessageEvent(Message message) { this.message = message; }
    @Override public final void dispatch(EventListener listener)
    {
        if (!(listener instanceof ChatListener) || message == null) return;
        ChatListener target = (ChatListener) listener;
        target.onMessage(message);
        MessageType type = message.getType();
        if (type == null) return;
        switch (type)
        {
            case AUTO: case MOD_AUTO: target.onAutoMessage(message); break;
            case PRIVATE_INFO: target.onPrivateInfoMessage(message); break;
            case PRIVATE_RECV: case MOD_PRIVATE_CHAT: target.onPrivateInMessage(message); break;
            case PRIVATE_SENT: target.onPrivateOutMessage(message); break;
            case PLAYER: case MOD_CHAT: target.onPlayerMessage(message); break;
            case TRADE: case TRADE_SENT: case TRADE_COMPLETE: target.onTradeMessage(message); break;
            case CLAN_CHAT: case CLAN_MESSAGE: case CLAN_GUEST_CHAT: case CLAN_GUEST_MESSAGE:
                target.onClanMessage(message); break;
            default: target.onGameMessage(message); break;
        }
    }
}
