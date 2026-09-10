package com.dreambotreborn.api.methods.friend;

public class Friend extends NameProvider
{
    private final int world;
    Friend(net.runelite.api.Friend friend)
    {
        super(friend == null ? "" : friend.getName(), friend == null ? "" : friend.getPrevName());
        world = friend == null ? 0 : friend.getWorld();
    }
    public boolean isOnline() { return world > 0; }
    public boolean isInMyWorld()
    {
        return isOnline() && world == com.dreambotreborn.api.methods.world.Worlds.getCurrentWorld();
    }
    public int getWorld() { return world; }
    public boolean sendMessage(String message) { return Friends.sendMessage(getName(), message); }
}
