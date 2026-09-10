package com.dreambotreborn.api.script;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EventListener;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.data.GameState;
import com.dreambotreborn.api.internal.Containers;
import com.dreambotreborn.api.methods.container.impl.ContainerType;
import com.dreambotreborn.api.methods.skills.Skill;
import com.dreambotreborn.api.script.event.ScriptEvent;
import com.dreambotreborn.api.script.event.impl.ActionEvent;
import com.dreambotreborn.api.script.event.impl.BankItemEvent;
import com.dreambotreborn.api.script.event.impl.EquipmentItemEvent;
import com.dreambotreborn.api.script.event.impl.EntitySpawnEvent;
import com.dreambotreborn.api.script.event.impl.ExperienceEvent;
import com.dreambotreborn.api.script.event.impl.GameObjectSpawnEvent;
import com.dreambotreborn.api.script.event.impl.GameStateEvent;
import com.dreambotreborn.api.script.event.impl.GroundItemSpawnEvent;
import com.dreambotreborn.api.script.event.impl.HitSplatEvent;
import com.dreambotreborn.api.script.event.impl.InventoryItemEvent;
import com.dreambotreborn.api.script.event.impl.MenuAddedEvent;
import com.dreambotreborn.api.script.event.impl.MessageEvent;
import com.dreambotreborn.api.script.event.impl.PlayerAnimationEvent;
import com.dreambotreborn.api.script.event.impl.NpcAnimationEvent;
import com.dreambotreborn.api.script.event.impl.ProjectileSpawnEvent;
import com.dreambotreborn.api.script.event.impl.ProjectileTargetEvent;
import com.dreambotreborn.api.script.event.impl.WidgetDecodedEvent;
import com.dreambotreborn.api.script.event.impl.VarBitEvent;
import com.dreambotreborn.api.script.event.impl.VarpEvent;
import com.dreambotreborn.api.script.listener.AnimationListener;
import com.dreambotreborn.api.script.listener.ChatListener;
import com.dreambotreborn.api.script.listener.ClientEventListener;
import com.dreambotreborn.api.script.listener.GameStateListener;
import com.dreambotreborn.api.script.listener.GameTickListener;
import com.dreambotreborn.api.script.listener.HitSplatListener;
import com.dreambotreborn.api.script.listener.HumanMouseListener;
import com.dreambotreborn.api.script.listener.ItemContainerListener;
import com.dreambotreborn.api.script.listener.KeyboardListener;
import com.dreambotreborn.api.script.listener.MenuRowListener;
import com.dreambotreborn.api.script.listener.ProjectileListener;
import com.dreambotreborn.api.script.listener.SpawnListener;
import com.dreambotreborn.api.script.listener.VarListener;
import com.dreambotreborn.api.script.listener.WidgetEventListener;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;

/** Dispatches injected-client and human-input events to the running script and solvers. */
public final class ScriptEventBus
{
    private static final ScriptEventBus INSTANCE = new ScriptEventBus();
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();
    private final Map<net.runelite.api.Actor, Integer> animations =
        Collections.synchronizedMap(new IdentityHashMap<>());
    private final Set<net.runelite.api.Projectile> projectiles =
        Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
    private final Map<net.runelite.api.Skill, Integer> experience =
        Collections.synchronizedMap(new EnumMap<>(net.runelite.api.Skill.class));
    private final Map<net.runelite.api.Skill, Integer> levels =
        Collections.synchronizedMap(new EnumMap<>(net.runelite.api.Skill.class));
    private final Map<ContainerType, List<com.dreambotreborn.api.wrappers.items.Item>> containers =
        Collections.synchronizedMap(new EnumMap<>(ContainerType.class));

    private ScriptEventBus()
    {
    }

    public static ScriptEventBus getInstance()
    {
        return INSTANCE;
    }

    public void register(EventListener listener)
    {
        if (listener != null) listeners.addIfAbsent(listener);
    }

    public void unregister(EventListener listener)
    {
        listeners.remove(listener);
    }

    public void clearRegisteredListeners()
    {
        listeners.clear();
    }

    /** Clears per-client deltas while preserving explicitly registered listeners. */
    public void resetClientState()
    {
        animations.clear();
        projectiles.clear();
        experience.clear();
        levels.clear();
        containers.clear();
    }

    public void post(Object event)
    {
        if (event == null) return;
        List<ScriptEvent> compatibilityEvents = compatibilityEvents(event);
        for (EventListener listener : targets())
        {
            try
            {
                dispatch(listener, event);
                for (ScriptEvent compatibilityEvent : compatibilityEvents)
                    compatibilityEvent.dispatch(listener);
            }
            catch (Throwable error)
            {
                ScriptManager.getScriptManager().reportEventFailure(listener, error);
            }
        }
    }

    public void postServerTick()
    {
        for (EventListener listener : targets())
        {
            if (listener instanceof GameTickListener)
            {
                try { ((GameTickListener) listener).onServerTick(); }
                catch (Throwable error) { ScriptManager.getScriptManager().reportEventFailure(listener, error); }
            }
        }
    }

    public void postHumanMouse(MouseEvent event)
    {
        if (event == null) return;
        for (EventListener listener : targets())
        {
            if (!(listener instanceof HumanMouseListener)) continue;
            HumanMouseListener human = (HumanMouseListener) listener;
            try
            {
                switch (event.getID())
                {
                    case MouseEvent.MOUSE_CLICKED: human.onMouseClicked(event); break;
                    case MouseEvent.MOUSE_PRESSED: human.onMousePressed(event); break;
                    case MouseEvent.MOUSE_RELEASED: human.onMouseReleased(event); break;
                    case MouseEvent.MOUSE_ENTERED: human.onMouseEntered(event); break;
                    case MouseEvent.MOUSE_EXITED: human.onMouseExited(event); break;
                    case MouseEvent.MOUSE_DRAGGED: human.onMouseDragged(event); break;
                    case MouseEvent.MOUSE_MOVED: human.onMouseMoved(event); break;
                    case MouseEvent.MOUSE_WHEEL:
                        if (event instanceof MouseWheelEvent) human.onMouseWheelMoved((MouseWheelEvent) event);
                        break;
                    default: break;
                }
            }
            catch (Throwable error) { ScriptManager.getScriptManager().reportEventFailure(listener, error); }
        }
    }

    public void postHumanKeyboard(KeyEvent event)
    {
        if (event == null) return;
        for (EventListener listener : targets())
        {
            if (!(listener instanceof KeyboardListener)) continue;
            KeyboardListener keyboard = (KeyboardListener) listener;
            try
            {
                if (event.getID() == KeyEvent.KEY_PRESSED) keyboard.onKeyPressed(event);
                else if (event.getID() == KeyEvent.KEY_RELEASED) keyboard.onKeyReleased(event);
                else if (event.getID() == KeyEvent.KEY_TYPED) keyboard.onKeyTyped(event);
            }
            catch (Throwable error) { ScriptManager.getScriptManager().reportEventFailure(listener, error); }
        }
    }

    private List<EventListener> targets()
    {
        List<EventListener> result = new ArrayList<>(listeners);
        AbstractScript script = ScriptManager.getScriptManager().getCurrentScript();
        if (script != null && !result.contains(script)) result.add(script);
        com.dreambotreborn.api.randoms.RandomSolver solver = ScriptManager.getScriptManager()
            .getRandomManager().getCurrentSolver();
        if (solver != null && !result.contains(solver)) result.add(solver);
        return result;
    }

    private static void dispatch(EventListener listener, Object event)
    {
        if (listener instanceof ClientEventListener)
            ((ClientEventListener) listener).onClientEvent(event);
        if (event instanceof ScriptEvent)
        {
            ((ScriptEvent) event).dispatch(listener);
            return;
        }
        if (event instanceof GameTick && listener instanceof GameTickListener)
            ((GameTickListener) listener).onGameTick();
        else if (event instanceof ClientTick && listener instanceof GameTickListener)
            ((GameTickListener) listener).onPreTick();
        else if (event instanceof GameStateChanged && listener instanceof GameStateListener)
            ((GameStateListener) listener).onGameStateChange(((GameStateChanged) event).getGameState());
        else if (event instanceof ChatMessage && listener instanceof ChatListener)
            ((ChatListener) listener).onMessage((ChatMessage) event);
        else if (event instanceof ItemContainerChanged && listener instanceof ItemContainerListener)
            ((ItemContainerListener) listener).onItemContainerChanged((ItemContainerChanged) event);
        else if (event instanceof AnimationChanged && listener instanceof AnimationListener)
            ((AnimationListener) listener).onAnimationChanged((AnimationChanged) event);
        else if (event instanceof HitsplatApplied && listener instanceof HitSplatListener)
            ((HitSplatListener) listener).onHitSplatAdded((HitsplatApplied) event);
        else if (event instanceof VarbitChanged && listener instanceof VarListener)
            ((VarListener) listener).onVarBitUpdate((VarbitChanged) event);
        else if (event instanceof WidgetLoaded && listener instanceof WidgetEventListener)
            ((WidgetEventListener) listener).onWidgetLoaded((WidgetLoaded) event);
        else if (event instanceof WidgetClosed && listener instanceof WidgetEventListener)
            ((WidgetEventListener) listener).onWidgetClosed((WidgetClosed) event);
        else if (event instanceof MenuEntryAdded && listener instanceof MenuRowListener)
            ((MenuRowListener) listener).onRowAdded((MenuEntryAdded) event);
        else if (event instanceof MenuOptionClicked && listener instanceof MenuRowListener)
            ((MenuRowListener) listener).onAction((MenuOptionClicked) event);
        else if (event instanceof ProjectileMoved && listener instanceof ProjectileListener)
            ((ProjectileListener) listener).onProjectileMoved((ProjectileMoved) event);
        else if (listener instanceof SpawnListener)
            INSTANCE.dispatchSpawn((SpawnListener) listener, event);
    }

    private void dispatchSpawn(SpawnListener listener, Object event)
    {
        if (event instanceof GameObjectSpawned)
        {
            GameObjectSpawned value = (GameObjectSpawned) event;
            listener.onGameObjectSpawn(value);
        }
        else if (event instanceof GameObjectDespawned)
        {
            GameObjectDespawned value = (GameObjectDespawned) event;
            listener.onGameObjectDespawn(value);
        }
        else if (event instanceof ItemSpawned)
        {
            ItemSpawned value = (ItemSpawned) event;
            listener.onGroundItemSpawn(value);
        }
        else if (event instanceof ItemDespawned)
        {
            ItemDespawned value = (ItemDespawned) event;
            listener.onGroundItemDespawn(value);
        }
        else if (event instanceof ItemQuantityChanged)
        {
            ItemQuantityChanged value = (ItemQuantityChanged) event;
            listener.onGroundItemUpdate(value);
        }
        else if (event instanceof NpcSpawned)
        {
            NpcSpawned value = (NpcSpawned) event;
            listener.onNpcSpawn(value);
        }
        else if (event instanceof NpcDespawned)
        {
            NpcDespawned value = (NpcDespawned) event;
            listener.onNpcDespawn(value);
        }
        else if (event instanceof PlayerSpawned)
        {
            PlayerSpawned value = (PlayerSpawned) event;
            listener.onPlayerSpawn(value);
        }
        else if (event instanceof PlayerDespawned)
        {
            PlayerDespawned value = (PlayerDespawned) event;
            listener.onPlayerDespawn(value);
        }
    }

    /** Creates compatibility snapshots once so every listener sees identical event data. */
    private List<ScriptEvent> compatibilityEvents(Object event)
    {
        List<ScriptEvent> result = new ArrayList<>();
        if (event instanceof ScriptEvent) return result;
        if (event instanceof GameStateChanged)
        {
            result.add(new GameStateEvent(GameState.fromRuneLite(
                ((GameStateChanged) event).getGameState())));
        }
        else if (event instanceof ChatMessage)
        {
            ChatMessage chat = (ChatMessage) event;
            result.add(new MessageEvent(new com.dreambotreborn.api.wrappers.widgets.message.Message(
                chat.getType() == null ? -1 : chat.getType().getType(), chat.getName(),
                chat.getMessage(), chat.getTimestamp())));
        }
        else if (event instanceof ItemContainerChanged)
        {
            result.addAll(containerEvents((ItemContainerChanged) event));
        }
        else if (event instanceof AnimationChanged)
        {
            net.runelite.api.Actor actor = ((AnimationChanged) event).getActor();
            if (actor != null)
            {
                int current = actor.getAnimation();
                Integer known = animations.put(actor, current);
                int previous = known == null ? current : known;
                if (actor instanceof net.runelite.api.Player)
                    result.add(new PlayerAnimationEvent(wrap((net.runelite.api.Player) actor),
                        previous, current));
                else if (actor instanceof net.runelite.api.NPC)
                    result.add(new NpcAnimationEvent(wrap((net.runelite.api.NPC) actor),
                        ((net.runelite.api.NPC) actor).getIndex(), previous, current));
            }
        }
        else if (event instanceof HitsplatApplied)
        {
            HitsplatApplied applied = (HitsplatApplied) event;
            net.runelite.api.Hitsplat hit = applied.getHitsplat();
            com.dreambotreborn.api.wrappers.interactive.Entity entity = wrap(applied.getActor());
            if (hit != null && entity != null)
                result.add(new HitSplatEvent(entity, hit.getHitsplatType(), hit.getAmount(),
                    hit.getDisappearsOnGameCycle(), -1, 0));
        }
        else if (event instanceof VarbitChanged)
        {
            VarbitChanged changed = (VarbitChanged) event;
            if (changed.getVarbitId() >= 0)
                result.add(new VarBitEvent(changed.getVarbitId(), changed.getValue(), changed.getValue()));
            if (changed.getVarpId() >= 0)
                result.add(new VarpEvent(changed.getVarpId(), changed.getValue(), changed.getValue()));
        }
        else if (event instanceof StatChanged)
        {
            StatChanged changed = (StatChanged) event;
            Skill skill = skill(changed.getSkill());
            Integer previousXp = experience.put(changed.getSkill(), changed.getXp());
            Integer previousLevel = levels.put(changed.getSkill(), changed.getLevel());
            if (skill != null && previousXp != null && changed.getXp() > previousXp)
                result.add(new ExperienceEvent(ExperienceEvent.GAINED,
                    changed.getXp() - previousXp, skill));
            if (skill != null && previousLevel != null && changed.getLevel() != previousLevel)
                result.add(new ExperienceEvent(changed.getLevel() > previousLevel
                    ? ExperienceEvent.LEVEL_UP : ExperienceEvent.LEVEL_CHANGE,
                    changed.getLevel() - previousLevel, skill));
        }
        else if (event instanceof WidgetLoaded)
        {
            WidgetLoaded loaded = (WidgetLoaded) event;
            com.dreambotreborn.api.wrappers.widgets.WidgetChild root =
                com.dreambotreborn.api.methods.widget.Widgets.get(loaded.getGroupId(), 0);
            if (root != null) result.add(new WidgetDecodedEvent(root));
        }
        else if (event instanceof MenuEntryAdded)
        {
            MenuEntryAdded added = (MenuEntryAdded) event;
            result.add(new MenuAddedEvent(new com.dreambotreborn.api.wrappers.widgets.MenuRow(
                added.getOption(), added.getTarget(), added.getIdentifier(),
                added.getActionParam0(), added.getActionParam1(), added.getType(), added.getItemId())));
        }
        else if (event instanceof MenuOptionClicked)
        {
            MenuOptionClicked clicked = (MenuOptionClicked) event;
            net.runelite.api.Point mouse = DreamBotRebornApi.requireClient().getMouseCanvasPosition();
            result.add(new ActionEvent(clicked.getMenuAction() == null ? -1
                : clicked.getMenuAction().getId(), clicked.getId(), clicked.getParam0(),
                clicked.getParam1(), clicked.getId(), clicked.getItemId(), clicked.getMenuOption(),
                clicked.getMenuTarget(), mouse == null ? -1 : mouse.getX(),
                mouse == null ? -1 : mouse.getY()));
        }
        else if (event instanceof ProjectileMoved)
        {
            net.runelite.api.Projectile value = ((ProjectileMoved) event).getProjectile();
            if (value != null)
            {
                com.dreambotreborn.api.wrappers.graphics.Projectile projectile =
                    new com.dreambotreborn.api.wrappers.graphics.Projectile(value);
                result.add(new ProjectileTargetEvent(projectile, false));
                if (projectiles.add(value)) result.add(new ProjectileSpawnEvent(projectile));
                if (projectiles.size() > 2048) projectiles.clear();
            }
        }
        else if (event instanceof GameObjectSpawned)
            result.add(new GameObjectSpawnEvent(wrap(((GameObjectSpawned) event).getGameObject()), true));
        else if (event instanceof GameObjectDespawned)
            result.add(new GameObjectSpawnEvent(wrap(((GameObjectDespawned) event).getGameObject()), false));
        else if (event instanceof ItemSpawned)
        {
            ItemSpawned value = (ItemSpawned) event;
            result.add(new GroundItemSpawnEvent(wrap(value.getItem(), value.getTile()), true));
        }
        else if (event instanceof ItemDespawned)
        {
            ItemDespawned value = (ItemDespawned) event;
            result.add(new GroundItemSpawnEvent(wrap(value.getItem(), value.getTile()), false));
        }
        else if (event instanceof ItemQuantityChanged)
        {
            ItemQuantityChanged value = (ItemQuantityChanged) event;
            result.add(new GroundItemSpawnEvent(wrap(value.getItem(), value.getTile()), true, true));
        }
        else if (event instanceof NpcSpawned)
            result.add(new EntitySpawnEvent(wrap(((NpcSpawned) event).getNpc()), true));
        else if (event instanceof NpcDespawned)
            result.add(new EntitySpawnEvent(wrap(((NpcDespawned) event).getNpc()), false));
        else if (event instanceof PlayerSpawned)
            result.add(new EntitySpawnEvent(wrap(((PlayerSpawned) event).getPlayer()), true));
        else if (event instanceof PlayerDespawned)
            result.add(new EntitySpawnEvent(wrap(((PlayerDespawned) event).getPlayer()), false));
        return result;
    }

    private List<ScriptEvent> containerEvents(ItemContainerChanged event)
    {
        ContainerType type = containerType(event.getContainerId());
        if (type == null) return Collections.emptyList();
        List<com.dreambotreborn.api.wrappers.items.Item> current =
            new ArrayList<>(Containers.items(type));
        List<com.dreambotreborn.api.wrappers.items.Item> previous = containers.put(type, current);
        if (previous == null) return Collections.emptyList();
        Map<Integer, com.dreambotreborn.api.wrappers.items.Item> before = bySlot(previous);
        Map<Integer, com.dreambotreborn.api.wrappers.items.Item> after = bySlot(current);
        java.util.TreeSet<Integer> slots = new java.util.TreeSet<>(before.keySet());
        slots.addAll(after.keySet());
        List<ScriptEvent> result = new ArrayList<>();
        for (Integer slot : slots)
        {
            com.dreambotreborn.api.wrappers.items.Item oldItem = before.get(slot);
            com.dreambotreborn.api.wrappers.items.Item newItem = after.get(slot);
            if (sameItem(oldItem, newItem)) continue;
            if (type == ContainerType.INVENTORY)
                result.add(new InventoryItemEvent(oldItem, newItem));
            else if (type == ContainerType.EQUIPMENT)
                result.add(new EquipmentItemEvent(oldItem, newItem));
            else if (type == ContainerType.BANK)
                result.add(new BankItemEvent(oldItem, newItem));
        }
        return result;
    }

    private static Map<Integer, com.dreambotreborn.api.wrappers.items.Item> bySlot(
        List<com.dreambotreborn.api.wrappers.items.Item> items)
    {
        Map<Integer, com.dreambotreborn.api.wrappers.items.Item> result = new java.util.HashMap<>();
        for (com.dreambotreborn.api.wrappers.items.Item item : items)
            if (item != null) result.put(item.getSlot(), item);
        return result;
    }

    private static boolean sameItem(com.dreambotreborn.api.wrappers.items.Item first,
                                    com.dreambotreborn.api.wrappers.items.Item second)
    {
        return first == second || first != null && second != null
            && first.getId() == second.getId() && first.getAmount() == second.getAmount();
    }

    private static ContainerType containerType(int id)
    {
        if (id == net.runelite.api.InventoryID.INVENTORY.getId()) return ContainerType.INVENTORY;
        if (id == net.runelite.api.InventoryID.EQUIPMENT.getId()) return ContainerType.EQUIPMENT;
        if (id == net.runelite.api.InventoryID.BANK.getId()) return ContainerType.BANK;
        return null;
    }

    private static com.dreambotreborn.api.wrappers.interactive.GameObject wrap(
        net.runelite.api.GameObject value)
    {
        if (value == null) return null;
        net.runelite.api.ObjectComposition definition =
            DreamBotRebornApi.requireClient().getObjectDefinition(value.getId());
        if (definition != null && definition.getImpostorIds() != null
            && definition.getImpostor() != null) definition = definition.getImpostor();
        return new com.dreambotreborn.api.wrappers.interactive.SceneObject(value);
    }

    private static com.dreambotreborn.api.wrappers.items.GroundItem wrap(
        net.runelite.api.TileItem value, net.runelite.api.Tile tile)
    {
        if (value == null || tile == null) return null;
        net.runelite.api.ItemComposition definition =
            DreamBotRebornApi.requireClient().getItemDefinition(value.getId());
        return new com.dreambotreborn.api.wrappers.items.GroundItem(value, tile,
            definition == null ? "" : definition.getName());
    }

    private static com.dreambotreborn.api.wrappers.interactive.NPC wrap(net.runelite.api.NPC value)
    {
        if (value == null) return null;
        net.runelite.api.NPCComposition definition = value.getTransformedComposition();
        if (definition == null) definition = value.getComposition();
        return new com.dreambotreborn.api.wrappers.interactive.NPC(value, definition);
    }

    private static com.dreambotreborn.api.wrappers.interactive.Player wrap(net.runelite.api.Player value)
    {
        if (value == null) return null;
        return new com.dreambotreborn.api.wrappers.interactive.Player(value,
            DreamBotRebornApi.requireClient().getPlayerOptions(),
            value == DreamBotRebornApi.requireClient().getLocalPlayer());
    }

    private static com.dreambotreborn.api.wrappers.interactive.Entity wrap(net.runelite.api.Actor value)
    {
        if (value instanceof net.runelite.api.Player) return wrap((net.runelite.api.Player) value);
        if (value instanceof net.runelite.api.NPC) return wrap((net.runelite.api.NPC) value);
        return null;
    }

    private static Skill skill(net.runelite.api.Skill value)
    {
        if (value == null) return null;
        String name = value == net.runelite.api.Skill.RUNECRAFT ? "RUNECRAFTING" : value.name();
        try { return Skill.valueOf(name); }
        catch (IllegalArgumentException absent) { return null; }
    }
}
