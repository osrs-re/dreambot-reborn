package com.dreambotreborn.api.compat;

import java.util.concurrent.atomic.AtomicInteger;
import com.dreambotreborn.api.data.requirements.ConditionalRequirement;
import com.dreambotreborn.api.data.requirements.LogicType;
import com.dreambotreborn.api.data.requirements.Requirement;
import com.dreambotreborn.api.methods.fairyring.FairyLocation;
import com.dreambotreborn.api.methods.magic.Ancient;
import com.dreambotreborn.api.methods.magic.Arceuus;
import com.dreambotreborn.api.methods.magic.Lunar;
import com.dreambotreborn.api.methods.magic.Normal;
import com.dreambotreborn.api.script.event.impl.MessageEvent;
import com.dreambotreborn.api.script.event.impl.VarBitEvent;
import com.dreambotreborn.api.script.listener.ChatListener;
import com.dreambotreborn.api.script.listener.VarListener;
import com.dreambotreborn.api.wrappers.widgets.message.Message;
import com.dreambotreborn.api.wrappers.widgets.message.MessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptEventCompatibilityTest
{
    @Test
    void messageEventsDispatchGeneralAndSpecificCallbacks()
    {
        AtomicInteger calls = new AtomicInteger();
        ChatListener listener = new ChatListener()
        {
            @Override public void onMessage(Message message) { calls.incrementAndGet(); }
            @Override public void onPlayerMessage(Message message) { calls.incrementAndGet(); }
        };
        new MessageEvent(new Message(MessageType.PLAYER.getId(), "Alice", "Hello", 1))
            .dispatch(listener);
        assertEquals(2, calls.get());
    }

    @Test
    void varEventsDispatchBothCompatibilityOverloads()
    {
        AtomicInteger calls = new AtomicInteger();
        VarListener listener = new VarListener()
        {
            @Override public void onVarBitUpdate(int id, int previous, int current)
            { assertEquals(10, id); assertEquals(1, previous); assertEquals(2, current); calls.incrementAndGet(); }
            @Override public void onVarBitUpdate(int id, int current) { calls.incrementAndGet(); }
        };
        new VarBitEvent(10, 1, 2).dispatch(listener);
        assertEquals(2, calls.get());
    }

    @Test
    void referenceSpellExperienceTablesAreComplete()
    {
        for (Normal spell : Normal.values()) assertTrue(spell.getExperience() >= 0);
        for (Ancient spell : Ancient.values()) assertTrue(spell.getExp() >= 0);
        for (Lunar spell : Lunar.values()) assertTrue(spell.getExperience() >= 0);
        for (Arceuus spell : Arceuus.values())
        {
            assertTrue(spell.getMagicExp() >= 0);
            assertTrue(spell.getPrayerExp() >= 0);
        }
        assertEquals(52, Ancient.ICE_BARRAGE.getExp());
        assertEquals(112, Lunar.VENGEANCE.getExperience());
        assertEquals(1500, Arceuus.MASTER_REANIMATION.getPrayerExp());
    }

    @Test
    void fairyLocationsExposeDefensiveCodeCopies()
    {
        assertArrayEquals(new String[] {"D", "K", "R"}, FairyLocation.EDGEVILLE.getCode());
        String[] mutable = FairyLocation.EDGEVILLE.getCode();
        mutable[0] = "A";
        assertArrayEquals(new String[] {"D", "K", "R"}, FairyLocation.EDGEVILLE.getCode());
    }

    @Test
    void conditionalRequirementsComposeWithoutClientState()
    {
        Requirement yes = new Requirement() { @Override public boolean meetsRequirement() { return true; } };
        Requirement no = new Requirement() { @Override public boolean meetsRequirement() { return false; } };
        assertTrue(new ConditionalRequirement(LogicType.AND, yes, yes).meetsRequirement());
        assertTrue(new ConditionalRequirement(LogicType.OR, no, yes).meetsRequirement());
        assertTrue(new ConditionalRequirement(LogicType.NOT, no).meetsRequirement());
        assertFalse(new ConditionalRequirement(LogicType.NOT, yes).meetsRequirement());
    }
}
