package com.dreambotreborn.api.compat;

import java.lang.reflect.Method;
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

class DreamBotCompatibilityTest
{
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
