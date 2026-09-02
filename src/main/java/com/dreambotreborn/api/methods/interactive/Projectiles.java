package com.dreambotreborn.api.methods.interactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.methods.filter.Filter;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.wrappers.graphics.Projectile;
import com.dreambotreborn.api.wrappers.interactive.NPC;
import com.dreambotreborn.api.wrappers.interactive.Player;

/** DreamBot-style snapshot queries for projectiles. */
public final class Projectiles
{
    private static volatile List<Projectile> snapshot = Collections.emptyList();
    private static volatile boolean withCycleCheck = true;

    private Projectiles() { }
    public static List<Projectile> all() { return snapshot; }
    public static List<Projectile> all(Integer... ids) { return all(value -> matches(value.getId(), ids)); }
    public static List<Projectile> all(Filter<Projectile> filter)
    {
        Objects.requireNonNull(filter, "filter");
        List<Projectile> result = new ArrayList<>();
        for (Projectile value : snapshot) if (filter.match(value)) result.add(value);
        return Collections.unmodifiableList(result);
    }
    public static Projectile closest(Integer... ids) { return closest(value -> matches(value.getId(), ids)); }
    public static Projectile closest(Filter<Projectile> filter) { return closest(filter, localTile()); }
    public static Projectile closest(Filter<Projectile> filter, Tile origin)
    {
        Projectile closest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (Projectile value : snapshot)
        {
            Tile tile = value.getTile();
            if (!filter.match(value) || tile == null) continue;
            double candidate = origin == null ? 0 : tile.distance(origin);
            if (closest == null || candidate < distance) { closest = value; distance = candidate; }
        }
        return closest;
    }
    public static boolean playerIsTargeted(Player player)
    {
        if (player == null) return false;
        for (Projectile projectile : snapshot)
            if (projectile.getTargetActor() == player.unwrapActor()) return true;
        return false;
    }
    public static boolean npcIsTargeted(NPC npc)
    {
        if (npc == null) return false;
        for (Projectile projectile : snapshot)
            if (projectile.getTargetActor() == npc.unwrapActor()) return true;
        return false;
    }
    public static boolean isWithCycleCheck() { return withCycleCheck; }
    public static void setWithCycleCheck(boolean value) { withCycleCheck = value; }

    public static void refresh()
    {
        net.runelite.api.Deque<net.runelite.api.Projectile> values = DreamBotRebornApi.requireClient().getProjectiles();
        List<Projectile> result = new ArrayList<>();
        if (values != null) for (net.runelite.api.Projectile value : values)
            if (value != null && (!withCycleCheck || value.getRemainingCycles() >= 0))
                result.add(new Projectile(value));
        snapshot = Collections.unmodifiableList(result);
    }
    public static void clear() { snapshot = Collections.emptyList(); }
    private static Tile localTile()
    {
        net.runelite.api.Player local = DreamBotRebornApi.requireClient().getLocalPlayer();
        return local == null ? null : new Tile(local.getWorldLocation());
    }
    private static boolean matches(int id, Integer... ids)
    {
        if (ids != null) for (Integer expected : ids) if (expected != null && expected == id) return true;
        return false;
    }
}
