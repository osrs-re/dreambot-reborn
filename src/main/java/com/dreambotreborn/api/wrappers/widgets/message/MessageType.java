package com.dreambotreborn.api.wrappers.widgets.message;

import net.runelite.api.ChatMessageType;

public enum MessageType
{
    GAME(ChatMessageType.GAMEMESSAGE), MOD_CHAT(ChatMessageType.MODCHAT),
    PLAYER(ChatMessageType.PUBLICCHAT), PRIVATE_RECV(ChatMessageType.PRIVATECHAT),
    ENGINE(ChatMessageType.ENGINE), PRIVATE_INFO(ChatMessageType.LOGINLOGOUTNOTIFICATION),
    PRIVATE_SENT(ChatMessageType.PRIVATECHATOUT), MOD_PRIVATE_CHAT(ChatMessageType.MODPRIVATECHAT),
    CHAT_CHANNEL(ChatMessageType.FRIENDSCHAT),
    FRIENDS_NOTIFICATION(ChatMessageType.FRIENDSCHATNOTIFICATION),
    TRADE_SENT(ChatMessageType.TRADE_SENT), BROADCAST(ChatMessageType.BROADCAST),
    FEEDBACK(ChatMessageType.SNAPSHOTFEEDBACK), ITEM_EXAMINE(ChatMessageType.ITEM_EXAMINE),
    NPC_EXAMINE(ChatMessageType.NPC_EXAMINE), OBJECT_EXAMINE(ChatMessageType.OBJECT_EXAMINE),
    NEW_FRIEND_NOTIFICATION(ChatMessageType.FRIENDNOTIFICATION),
    NEW_IGNORE_NOTIFICATION(ChatMessageType.IGNORENOTIFICATION),
    CLAN_CHAT(ChatMessageType.CLAN_CHAT), CLAN_MESSAGE(ChatMessageType.CLAN_MESSAGE),
    CLAN_GUEST_CHAT(ChatMessageType.CLAN_GUEST_CHAT),
    CLAN_GUEST_MESSAGE(ChatMessageType.CLAN_GUEST_MESSAGE),
    AUTO(ChatMessageType.AUTOTYPER), MOD_AUTO(ChatMessageType.MODAUTOTYPER),
    TRADE(ChatMessageType.TRADEREQ), TRADE_COMPLETE(ChatMessageType.TRADE),
    DUEL(ChatMessageType.CHALREQ_TRADE), FRIENDLY_DUEL(ChatMessageType.CHALREQ_FRIENDSCHAT),
    FILTERED(ChatMessageType.SPAM), TIMEOUT_MESSAGE(ChatMessageType.TENSECTIMEOUT),
    WELCOME(ChatMessageType.WELCOME), CLAN_CREATION_INVITE(ChatMessageType.CLAN_CREATION_INVITATION),
    CLAN_WARS_CHALLENGE(ChatMessageType.CHALREQ_CLANCHAT),
    CLAN_IRON_MAN_FORM_GROUP(ChatMessageType.CLAN_GIM_FORM_GROUP),
    CLAN_IRON_MAN_GROUP_WITH(ChatMessageType.CLAN_GIM_GROUP_WITH),
    DIALOG(ChatMessageType.DIALOG), MESSAGE_BOX(ChatMessageType.MESBOX);

    private final int id;
    MessageType(ChatMessageType type) { id = type.getType(); }
    public int getId() { return id; }
    public int getID() { return id; }
    public static MessageType getType(int id)
    {
        for (MessageType value : values()) if (value.id == id) return value;
        return null;
    }
}
