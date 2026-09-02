package com.dreambotreborn.api.wrappers.widgets.message;

import java.util.Objects;

public class Message
{
    private final int typeId;
    private final String username;
    private final String message;
    private final int time;
    public Message(int typeId, String username, String message, int time)
    {
        this.typeId = typeId;
        this.username = clean(username);
        this.message = clean(message);
        this.time = time;
    }
    public String getUsername() { return username; }
    public MessageType getType() { return MessageType.getType(typeId); }
    public int getTypeID() { return typeId; }
    public String getMessage() { return message; }
    public int getTime() { return time; }
    @Override public String toString() { return username.isEmpty() ? message : username + ": " + message; }
    @Override public boolean equals(Object value)
    {
        if (!(value instanceof Message)) return false;
        Message other = (Message) value;
        return typeId == other.typeId && time == other.time
            && Objects.equals(username, other.username) && Objects.equals(message, other.message);
    }
    @Override public int hashCode() { return Objects.hash(typeId, username, message, time); }
    private static String clean(String value)
    {
        return value == null ? "" : value.replaceAll("<[^>]*>", "");
    }
}
