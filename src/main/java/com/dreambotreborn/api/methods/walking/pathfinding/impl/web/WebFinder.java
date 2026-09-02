package com.dreambotreborn.api.methods.walking.pathfinding.impl.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import com.dreambotreborn.api.methods.map.Tile;
import com.dreambotreborn.api.methods.walking.path.impl.GlobalPath;
import com.dreambotreborn.api.methods.walking.web.node.AbstractWebNode;
import com.dreambotreborn.api.methods.walking.web.node.WebNodeType;
import com.dreambotreborn.api.methods.walking.web.node.impl.BasicWebNode;

/** Local, extensible A* world-web implementation without DreamBot's retired service. */
public final class WebFinder
{
    private static final WebFinder INSTANCE = new WebFinder();
    private static volatile boolean loaded = true;
    private static volatile boolean debug;
    private static volatile String version = "dreambot-reborn-local-1";
    private final List<AbstractWebNode> nodes = new ArrayList<>();
    private final Set<AbstractWebNode> blacklist = new HashSet<>();
    private final EnumSet<WebNodeType> enabledTypes = EnumSet.allOf(WebNodeType.class);
    private volatile GlobalPath<AbstractWebNode> lastCalculatedPath = new GlobalPath<>();
    private volatile WebPathQuery currentPathQuery;
    private boolean equipTeleports = true;
    private boolean inventoryTeleports = true;
    private boolean equipmentTeleports = true;
    private double pathRandomization;

    private WebFinder() { }
    public static WebFinder getWebFinder() { return INSTANCE; }
    public static void loadWebNodes() { loaded = true; }
    public static boolean isLoaded() { return loaded; }
    public static void setWebNodeVersion(String value) { version = value == null ? "" : value; }
    public static String getWebNodeVersion() { return version; }
    public static boolean isWebNodeVersionAtLeast(String value)
    {
        return value == null || version.compareTo(value) >= 0;
    }
    public synchronized GlobalPath<AbstractWebNode> calculate(WebPathQuery query)
    {
        currentPathQuery = query;
        return calculate(query == null ? null : query.getFrom(), query == null ? null : query.getTo());
    }
    public GlobalPath<AbstractWebNode> calculate(int x1, int y1, int z1, int x2, int y2, int z2)
    {
        return calculate(new Tile(x1, y1, z1), new Tile(x2, y2, z2));
    }
    public synchronized GlobalPath<AbstractWebNode> calculate(Tile from, Tile to)
    {
        if (from == null || to == null || from.getZ() != to.getZ())
            return remember(Collections.emptyList(), 0);
        AbstractWebNode start = getNearestGlobal(from, 24);
        AbstractWebNode goal = getNearestGlobal(to, 24);
        List<AbstractWebNode> route = start == null || goal == null
            ? straightLine(from, to) : aStar(start, goal);
        if (route.isEmpty()) route = straightLine(from, to);
        if (route.isEmpty() || route.get(0).getTile().distance(from) > 0)
            route.add(0, new BasicWebNode(from.getX(), from.getY(), from.getZ()));
        if (route.get(route.size() - 1).getTile().distance(to) > 0)
            route.add(new BasicWebNode(to.getX(), to.getY(), to.getZ()));
        return remember(route, route.size());
    }
    public GlobalPath<AbstractWebNode> calculate(AbstractWebNode from, AbstractWebNode to)
    {
        return calculate(from, to, Integer.MAX_VALUE);
    }
    public synchronized GlobalPath<AbstractWebNode> calculate(
        AbstractWebNode from, AbstractWebNode to, int maximumCost)
    {
        List<AbstractWebNode> route = aStar(from, to);
        GlobalPath<AbstractWebNode> path = remember(route, route.size());
        return path.getFinalCost() <= maximumCost ? path : new GlobalPath<>();
    }
    public void disableWebNodeType(WebNodeType type) { if (type != null) enabledTypes.remove(type); }
    public boolean isWebNodeTypeEnabled(WebNodeType type) { return enabledTypes.contains(type); }
    public void enableWebNodeType(WebNodeType type) { if (type != null) enabledTypes.add(type); }
    public boolean canEquipTeleports() { return equipTeleports; }
    public void disableEquippingTeleports() { equipTeleports = false; }
    public void enableEquippingTeleports() { equipTeleports = true; }
    public boolean canUseInventoryTeleports() { return inventoryTeleports; }
    public boolean canUseEquipmentTeleports() { return equipmentTeleports; }
    public void disableInventoryTeleports() { inventoryTeleports = false; }
    public void enableInventoryTeleports() { inventoryTeleports = true; }
    public void disableEquipmentTeleports() { equipmentTeleports = false; }
    public void enableEquipmentTeleports() { equipmentTeleports = true; }
    public AbstractWebNode getNearest(AbstractWebNode node) { return getNearest(node, Integer.MAX_VALUE); }
    public AbstractWebNode getNearest(AbstractWebNode node, int distance)
    {
        return node == null ? null : getNearest(node.getTile(), distance);
    }
    public synchronized AbstractWebNode getNearest(Tile tile, int distance)
    {
        AbstractWebNode result = null; double best = distance;
        for (AbstractWebNode node : nodes)
        {
            double candidate = node.distance(tile);
            if (usable(node) && candidate <= best) { result = node; best = candidate; }
        }
        return result;
    }
    public boolean addNode(Tile tile) { return createAndAddNode(tile) != null; }
    public AbstractWebNode createAndAddNode(Tile tile)
    {
        if (tile == null) return null;
        BasicWebNode node = new BasicWebNode(tile.getX(), tile.getY(), tile.getZ());
        addWebNode(node); return node;
    }
    public AbstractWebNode getNearestGlobal(Tile tile, int distance) { return getNearest(tile, distance); }
    public synchronized ArrayList<AbstractWebNode> getNodesWithin(int distance, Tile tile)
    {
        ArrayList<AbstractWebNode> result = new ArrayList<>();
        for (AbstractWebNode node : nodes) if (node.distance(tile) <= distance) result.add(node);
        return result;
    }
    public synchronized void removeNode(int index)
    {
        if (index >= 0 && index < nodes.size()) removeNode(nodes.get(index));
    }
    public synchronized void removeNode(AbstractWebNode node)
    {
        nodes.remove(node); for (AbstractWebNode value : nodes) value.removeConnections(node);
    }
    public void addBlacklistedNode(AbstractWebNode node) { if (node != null) blacklist.add(node); }
    public void clearBlacklist() { blacklist.clear(); }
    public synchronized void clearCustomNodes() { nodes.clear(); }
    public synchronized int getId(AbstractWebNode node) { return nodes.indexOf(node); }
    public synchronized void addWebNode(AbstractWebNode node)
    {
        if (node == null || nodes.contains(node)) return;
        node.setIndex(nodes.size());
        AbstractWebNode nearest = getNearest(node.getTile(), 16);
        nodes.add(node);
        if (nearest != null) node.addDualConnections(nearest);
    }
    public void addWebNodes(AbstractWebNode... values)
    {
        if (values != null) for (AbstractWebNode value : values) addWebNode(value);
    }
    public synchronized AbstractWebNode get(int index)
    {
        return index < 0 || index >= nodes.size() ? null : nodes.get(index);
    }
    public void resetWebNodes() { clearCustomNodes(); }
    public GlobalPath<AbstractWebNode> getLastCalculatedPath() { return lastCalculatedPath; }
    public synchronized List<AbstractWebNode> getAll()
    {
        return Collections.unmodifiableList(new ArrayList<>(nodes));
    }
    public void setPathRandomization(double value) { pathRandomization = Math.max(0, value); }
    public static void toggleWebNodeDebug(boolean value) { debug = value; }
    public static boolean isWebNodeDebugEnabled() { return debug; }
    public double getPathRandomization() { return pathRandomization; }
    public WebPathQuery getCurrentPathQuery() { return currentPathQuery; }
    public void setCurrentPathQuery(WebPathQuery value) { currentPathQuery = value; }

    private boolean usable(AbstractWebNode node)
    {
        return node != null && !blacklist.contains(node) && enabledTypes.contains(node.getType())
            && node.isValid();
    }
    private List<AbstractWebNode> aStar(AbstractWebNode start, AbstractWebNode goal)
    {
        if (!usable(start) || !usable(goal)) return Collections.emptyList();
        Map<AbstractWebNode, Double> cost = new HashMap<>();
        Map<AbstractWebNode, AbstractWebNode> previous = new HashMap<>();
        PriorityQueue<AbstractWebNode> open = new PriorityQueue<>(Comparator.comparingDouble(
            node -> cost.getOrDefault(node, Double.POSITIVE_INFINITY) + node.distance(goal.getTile())));
        cost.put(start, 0.0); open.add(start);
        while (!open.isEmpty())
        {
            AbstractWebNode current = open.poll();
            if (current.equals(goal)) break;
            for (AbstractWebNode next : current.getConnections())
            {
                if (!usable(next)) continue;
                double candidate = cost.get(current) + current.distance(next.getTile()) + next.getWeight();
                if (candidate < cost.getOrDefault(next, Double.POSITIVE_INFINITY))
                {
                    cost.put(next, candidate); previous.put(next, current); open.remove(next); open.add(next);
                }
            }
        }
        if (!start.equals(goal) && !previous.containsKey(goal)) return Collections.emptyList();
        List<AbstractWebNode> result = new ArrayList<>();
        for (AbstractWebNode cursor = goal; cursor != null; cursor = previous.get(cursor)) result.add(cursor);
        Collections.reverse(result); return result;
    }
    private List<AbstractWebNode> straightLine(Tile from, Tile to)
    {
        List<AbstractWebNode> result = new ArrayList<>();
        int distance = (int) from.distance(to);
        int steps = Math.max(1, (int) Math.ceil(distance / 10.0));
        for (int step = 0; step <= steps; step++)
        {
            double ratio = (double) step / steps;
            int x = (int) Math.round(from.getX() + (to.getX() - from.getX()) * ratio);
            int y = (int) Math.round(from.getY() + (to.getY() - from.getY()) * ratio);
            result.add(new BasicWebNode(x, y, from.getZ()));
        }
        return result;
    }
    private GlobalPath<AbstractWebNode> remember(List<AbstractWebNode> values, int cost)
    {
        GlobalPath<AbstractWebNode> path = new GlobalPath<>(values); path.setFinalCost(cost);
        lastCalculatedPath = path; return path;
    }
}
