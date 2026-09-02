package com.dreambotreborn.api.methods.interactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.runelite.api.Client;
import net.runelite.api.NPCComposition;
import net.runelite.api.coords.WorldPoint;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.Query;
import com.dreambotreborn.api.internal.Queries;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;

/** DreamBot-style queries over NPCs in the loaded scene. */
public final class NPCs
{
    private static volatile List<NPC> snapshot = Collections.emptyList();

    private NPCs()
    {
    }

    public static Query<NPC> all()
    {
        return new Query<>(snapshot);
    }

    public static Query<NPC> all(String... names)
    {
        return find(npc -> Queries.name(npc.name, names));
    }

    public static Query<NPC> all(int... ids)
    {
        return find(npc -> Queries.id(npc.id, ids));
    }

    public static List<NPC> all(Predicate<? super NPC> predicate)
    {
        return find(predicate).all();
    }

    public static Query<NPC> find(Predicate<? super NPC> predicate)
    {
        return all().filter(Objects.requireNonNull(predicate, "predicate"));
    }

    public static NPC getAtIndex(int index)
    {
        return find(npc -> npc.index == index).first();
    }

    public static NPC closest()
    {
        return closest(npc -> true);
    }

    public static NPC closest(String... names)
    {
        return closest(npc -> Queries.name(npc.name, names));
    }

    public static NPC closest(int... ids)
    {
        return closest(npc -> Queries.id(npc.id, ids));
    }

    public static NPC closest(Predicate<? super NPC> predicate)
    {
        Player local = Players.getLocal();
        return closest(predicate, local == null ? null : local.worldLocation);
    }

    public static NPC closest(Predicate<? super NPC> predicate, WorldPoint origin)
    {
        Objects.requireNonNull(predicate, "predicate");
        NPC result = null;
        int distance = Integer.MAX_VALUE;
        for (NPC npc : snapshot)
        {
            if (predicate.test(npc))
            {
                int candidate = npc.distance(origin);
                if (result == null || candidate < distance)
                {
                    result = npc;
                    distance = candidate;
                }
            }
        }
        return result;
    }

    public static Query<NPC> getInteractingWith(int playerIndex)
    {
        Player player = Players.getAtIndex(playerIndex);
        net.runelite.api.Actor target = player == null ? null : player.unwrapActor();
        return find(npc -> target != null && npc.unwrapActor().getInteracting() == target);
    }

    public static void refresh()
    {
        Client client = DreamBotRebornApi.requireClient();
        List<NPC> npcs = new ArrayList<>();
        List<net.runelite.api.NPC> liveNpcs = client.getNpcs();
        if (liveNpcs != null)
        {
            for (net.runelite.api.NPC live : liveNpcs)
            {
                if (live == null)
                {
                    continue;
                }
                NPCComposition composition = live.getTransformedComposition();
                if (composition == null)
                {
                    composition = live.getComposition();
                }
                npcs.add(new NPC(live, composition));
            }
        }
        snapshot = Collections.unmodifiableList(npcs);
    }

    public static void clear()
    {
        snapshot = Collections.emptyList();
    }
}
