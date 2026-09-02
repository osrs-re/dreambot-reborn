package com.dreambotreborn.api.script;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;

/** Dispatches injected-client and human-input events to the running script and solvers. */
public final class ScriptEventBus
{
    private static final ScriptEventBus INSTANCE = new ScriptEventBus();
    private final CopyOnWriteArrayList<EventListener> listeners = new CopyOnWriteArrayList<>();

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

    public void post(Object event)
    {
        if (event == null) return;
        for (EventListener listener : targets())
        {
            try { dispatch(listener, event); }
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
            dispatchSpawn((SpawnListener) listener, event);
    }

    private static void dispatchSpawn(SpawnListener listener, Object event)
    {
        if (event instanceof GameObjectSpawned) listener.onGameObjectSpawn((GameObjectSpawned) event);
        else if (event instanceof GameObjectDespawned) listener.onGameObjectDespawn((GameObjectDespawned) event);
        else if (event instanceof ItemSpawned) listener.onGroundItemSpawn((ItemSpawned) event);
        else if (event instanceof ItemDespawned) listener.onGroundItemDespawn((ItemDespawned) event);
        else if (event instanceof ItemQuantityChanged) listener.onGroundItemUpdate((ItemQuantityChanged) event);
        else if (event instanceof NpcSpawned) listener.onNpcSpawn((NpcSpawned) event);
        else if (event instanceof NpcDespawned) listener.onNpcDespawn((NpcDespawned) event);
        else if (event instanceof PlayerSpawned) listener.onPlayerSpawn((PlayerSpawned) event);
        else if (event instanceof PlayerDespawned) listener.onPlayerDespawn((PlayerDespawned) event);
    }
}
