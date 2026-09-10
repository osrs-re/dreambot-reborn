package com.dreambotreborn.api.methods.friend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.methods.widget.Widgets;
import com.dreambotreborn.api.utilities.Await;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.wrappers.widgets.WidgetChild;

public final class Friends
{
    private static final Friends INSTANCE = new Friends();
    private Friends() { }
    public static Friends getFriendsInstance() { return INSTANCE; }
    public static Friend[] getFriends() { return all().toArray(new Friend[0]); }
    public static List<Friend> all()
    {
        net.runelite.api.FriendContainer container = DreamBotRebornApi.requireClient().getFriendContainer();
        net.runelite.api.Friend[] values = container == null ? null : container.getMembers();
        if (values == null) return Collections.emptyList();
        List<Friend> result = new ArrayList<>();
        for (net.runelite.api.Friend value : values) if (value != null) result.add(new Friend(value));
        return Collections.unmodifiableList(result);
    }
    public static boolean swapToFriends() { return Tabs.open(Tab.ACCOUNT_MANAGEMENT); }
    public static int getFriendsSize() { return getSize(); }
    public static int getSize()
    {
        net.runelite.api.FriendContainer container = DreamBotRebornApi.requireClient().getFriendContainer();
        return container == null ? 0 : container.getCount();
    }
    public static boolean isFriendsSelected() { return Tabs.isOpen(Tab.ACCOUNT_MANAGEMENT); }
    public static boolean haveFriend(String name) { return get(name) != null; }
    public static boolean sendMessage(String name, String message)
    {
        if (name == null || message == null || !swapToFriends()) return false;
        WidgetChild friend = Widgets.getMatchingWidget(widget -> widget.visible
            && (widget.text.equalsIgnoreCase(name) || widget.name.equalsIgnoreCase(name))
            && widget.hasAction("Message"));
        return friend != null && friend.interact("Message")
            && Await.success(com.dreambotreborn.api.input.Keyboard.type(message, true));
    }
    public static Friend getFriend(String name) { return get(name); }
    public static Friend get(String name)
    {
        if (name == null) return null;
        for (Friend friend : all()) if (friend.getName().equalsIgnoreCase(name)) return friend;
        return null;
    }
    public static boolean addFriend(String name)
    {
        if (name == null || name.trim().isEmpty() || !swapToFriends()) return false;
        WidgetChild add = Widgets.getMatchingWidget(widget -> widget.visible
            && (widget.hasAction("Add friend") || widget.text.equalsIgnoreCase("Add friend")));
        return add != null && (add.hasAction("Add friend") ? add.interact("Add friend") : add.click())
            && Await.success(com.dreambotreborn.api.input.Keyboard.type(name, true));
    }
    public static boolean addFriend(Player player) { return player != null && addFriend(player.getName()); }
    public static boolean deleteFriend(Friend friend)
    { return friend != null && deleteFriend(friend.getName()); }
    public static boolean deleteFriend(String name)
    {
        if (name == null || !swapToFriends()) return false;
        WidgetChild friend = Widgets.getMatchingWidget(widget -> widget.visible
            && (widget.text.equalsIgnoreCase(name) || widget.name.equalsIgnoreCase(name))
            && widget.hasAction("Delete"));
        return friend != null && friend.interact("Delete");
    }
}
