package com.dreambotreborn.api.compat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import com.dreambotreborn.api.Client;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.data.ActionMode;
import com.dreambotreborn.api.data.GameState;
import com.dreambotreborn.api.methods.container.impl.Inventory;
import com.dreambotreborn.api.methods.container.impl.Shop;
import com.dreambotreborn.api.methods.container.impl.bank.Bank;
import com.dreambotreborn.api.methods.container.impl.equipment.Equipment;
import com.dreambotreborn.api.methods.container.impl.equipment.EquipmentSlot;
import com.dreambotreborn.api.methods.dialogues.Dialogues;
import com.dreambotreborn.api.methods.emotes.Emote;
import com.dreambotreborn.api.methods.emotes.Emotes;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.tabs.Tab;
import com.dreambotreborn.api.methods.tabs.Tabs;
import com.dreambotreborn.api.methods.walking.impl.Walking;
import com.dreambotreborn.api.wrappers.widgets.MenuRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DreamBotCompatibilityTest
{
    @Test
    void queryRemainsChainableAndIsAlsoAReadOnlyList()
    {
        Query<Integer> query = new Query<>(Arrays.asList(1, 2, 3));
        List<Integer> legacyList = query;
        assertEquals(Arrays.asList(1, 2, 3), legacyList);
        assertEquals(2, query.filter(value -> value > 1).first());
        assertTrue(query.exists());
    }

    @Test
    void clientUsesDreamBotCompatibilityTypes() throws Exception
    {
        assertEquals(GameState.class, Client.class.getMethod("getGameState").getReturnType());
        assertEquals(Tile.class, Client.class.getMethod("getBase").getReturnType());
        assertEquals(Tile.class, Client.class.getMethod("getDestination").getReturnType());
        assertEquals(GameState.ENTER_AUTH,
            GameState.fromRuneLite(net.runelite.api.GameState.LOGIN_SCREEN_AUTHENTICATOR));
        assertEquals(4, ActionMode.RIGHT_CLICK_FOR_CLANMATES.getVarbitValue());
    }

    @Test
    void compatibilityFamiliesAddedByTheReferenceAuditExist() throws Exception
    {
        assertNotNull(Class.forName("com.dreambotreborn.api.ClientSettings"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.grandexchange.GrandExchange"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.quest.Quests"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.quest.book.PaidQuest"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.map.Map"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.friend.Friends"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.ignore.IgnoredProvider"));
        assertNotNull(Class.forName("com.dreambotreborn.api.wrappers.map.TileReference"));
        assertNotNull(Class.forName("com.dreambotreborn.api.wrappers.interactive.SceneObject"));
        assertNotNull(Class.forName("com.dreambotreborn.api.wrappers.interactive.BoundaryObject"));
        assertNotNull(Class.forName("com.dreambotreborn.api.wrappers.interactive.FloorDecoration"));
    }

    @Test
    void localPathFinderHonorsCompatibilityBlacklist()
    {
        int[][] flags = new int[8][8];
        com.dreambotreborn.api.methods.walking.pathfinding.impl.local.LocalPathFinder finder =
            new com.dreambotreborn.api.methods.walking.pathfinding.impl.local.LocalPathFinder();
        Tile blocked = new Tile(2, 1, 0);
        finder.addBlacklistedTile(blocked);
        List<Tile> path = finder.find(flags, 0, 0, 0,
            new Tile(1, 1, 0), new Tile(4, 1, 0));
        assertFalse(path.isEmpty());
        assertFalse(path.contains(blocked));
        assertTrue(finder.isBlacklisted(blocked));
        finder.clearBlacklist();
        assertFalse(finder.isBlacklisted(blocked));
    }
    @Test
    void commonScriptCallsHaveSynchronousBooleanSignatures() throws Exception
    {
        returnsBoolean(Bank.class, "open");
        returnsBoolean(Bank.class, "withdraw", String.class, int.class);
        returnsBoolean(Bank.class, "depositAllItems");
        returnsBoolean(Inventory.class, "interact", String.class, String.class);
        returnsBoolean(Inventory.class, "dropAllExcept", String[].class);
        returnsBoolean(Equipment.class, "unequip", EquipmentSlot.class);
        returnsBoolean(Dialogues.class, "clickContinue");
        returnsBoolean(Shop.class, "purchase", String.class, int.class);
        returnsBoolean(Emotes.class, "doEmote", Emote.class);
        returnsBoolean(Tabs.class, "open", Tab.class);
        returnsBoolean(Walking.class, "walk", Tile.class);
    }

    @Test
    void originalPackageFamiliesExist() throws Exception
    {
        assertNotNull(Class.forName(
            "com.dreambotreborn.api.wrappers.interactive.interact.Interactable"));
        assertNotNull(Class.forName("com.dreambotreborn.api.wrappers.widgets.WidgetChild"));
        assertNotNull(Class.forName("com.dreambotreborn.api.wrappers.widgets.Menu"));
        assertNotNull(Class.forName(
            "com.dreambotreborn.api.methods.walking.pathfinding.impl.web.WebFinder"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.magic.Magic"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.trade.Trade"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.depositbox.DepositBox"));
        assertNotNull(Class.forName("com.dreambotreborn.api.methods.login.LoginUtility"));
        assertNotNull(Class.forName(
            "com.dreambotreborn.api.methods.container.general.ItemContainers"));
        assertNotNull(Class.forName(
            "com.dreambotreborn.api.wrappers.widgets.message.Message"));
        assertNotNull(Class.forName(
            "com.dreambotreborn.api.wrappers.map.impl.CollisionMap"));
    }

    @Test
    void menuRowsPreserveActionCodesAndCloneIndependently()
    {
        MenuRow row = new MenuRow("Bank", "Bank booth", 100, 12, 34, 3, -1);
        MenuRow clone = row.clone();
        clone.setAction("Examine");
        assertEquals("Bank", row.getAction());
        assertEquals("Examine", clone.getAction());
        assertEquals(3, row.getOpCode());
        assertEquals(100, row.getID());
    }

    private static void returnsBoolean(Class<?> type, String name, Class<?>... arguments)
        throws Exception
    {
        Method method = type.getMethod(name, arguments);
        assertEquals(boolean.class, method.getReturnType(), method.toString());
    }
}
