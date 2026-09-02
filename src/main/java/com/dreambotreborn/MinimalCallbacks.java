package com.dreambotreborn;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.net.URI;
import java.util.List;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.input.Keyboard;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.script.ScriptEventBus;
import com.dreambotreborn.accounts.AccountLogin;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.devtools.WidgetInspector;
import com.dreambotreborn.settings.PreferredWorld;
import com.dreambotreborn.api.methods.login.LoginStats;
import net.runelite.api.MainBufferProvider;
import net.runelite.api.Renderable;
import net.runelite.api.hooks.Callbacks;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;

/** The injected client expects RuneLite callbacks, even when no plugins are used. */
final class MinimalCallbacks implements Callbacks
{
    private static final long API_REFRESH_INTERVAL_NANOS = 100_000_000L;

    private final Component imageObserver;
    private long nextApiRefresh;
    private boolean apiRefreshFailed;

    MinimalCallbacks(Component imageObserver)
    {
        this.imageObserver = imageObserver;
    }

    @Override
    public void post(Object event)
    {
        if (event instanceof net.runelite.api.events.GameStateChanged)
        {
            net.runelite.api.GameState state =
                ((net.runelite.api.events.GameStateChanged) event).getGameState();
            LoginStats.setGameState(state == null ? -1 : state.getState());
            if (state == net.runelite.api.GameState.LOGGED_IN)
            {
                LoginStats.setLoginCount(LoginStats.getLoginCount() + 1);
                LoginStats.setLoginTime(System.currentTimeMillis());
            }
            else if (state == net.runelite.api.GameState.LOGIN_SCREEN)
            {
                LoginStats.setLogoutTime(System.currentTimeMillis());
            }
        }
        VirtualMouse.onEvent(event);
        WidgetInspector.onEvent(event);
        DreamBotRebornApi.onEvent(event);
        ScriptEventBus.getInstance().post(event);
    }

    @Override
    public void postDeferred(Object event)
    {
        ScriptEventBus.getInstance().post(event);
    }

    @Override
    public void tick()
    {
        ClientThread.pump();
        PreferredWorld.tick();
        AccountLogin.tick();
        WidgetInspector.tick();
        VirtualMouse.tick();

        long now = System.nanoTime();
        if (now >= nextApiRefresh)
        {
            nextApiRefresh = now + API_REFRESH_INTERVAL_NANOS;
            try
            {
                DreamBotRebornApi.refresh();
                apiRefreshFailed = false;
            }
            catch (RuntimeException ex)
            {
                if (!apiRefreshFailed)
                {
                    System.err.println("Unable to refresh the DreamBot Reborn API snapshots");
                    ex.printStackTrace(System.err);
                    apiRefreshFailed = true;
                }
            }
        }
    }

    @Override
    public void tickEnd()
    {
    }

    @Override
    public void frame()
    {
    }

    @Override
    public void serverTick()
    {
        ScriptEventBus.getInstance().postServerTick();
    }

    @Override
    public void drawScene()
    {
    }

    @Override
    public void drawAboveOverheads()
    {
    }

    @Override
    public void draw(MainBufferProvider bufferProvider, Graphics graphics, int x, int y)
    {
        if (graphics != null)
        {
            Graphics bufferGraphics = bufferProvider.getImage().getGraphics();
            try
            {
                // RuneLite renders overlays into the main buffer before copying
                // it to the canvas. Painting the cursor directly on the canvas
                // allowed independent AWT repaints to erase it between frames.
                if (bufferGraphics instanceof java.awt.Graphics2D)
                {
                    ScriptManager.getScriptManager().paint((java.awt.Graphics2D) bufferGraphics);
                }
                WidgetInspector.draw(bufferGraphics);
                VirtualMouse.draw(bufferGraphics);
            }
            finally
            {
                bufferGraphics.dispose();
            }
            graphics.drawImage(bufferProvider.getImage(), x, y, imageObserver);
        }
    }

    @Override
    public void drawInterface(int interfaceId, List<WidgetItem> widgetItems)
    {
        WidgetInspector.observeInterface(interfaceId);
    }

    @Override
    public void drawLayer(Widget layer, List<WidgetItem> widgetItems)
    {
        WidgetInspector.observeLayer(layer);
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, false);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseEvent(event, true, true);
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, false);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseEvent(event, true, false);
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, false);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseEvent(event, true, false);
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, true);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseEvent(event, true, false);
    }

    @Override
    public MouseEvent mouseExited(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, false);
        if (!VirtualMouse.isSyntheticInputEvent())
        {
            WidgetInspector.clearPickerHover();
            event = WidgetInspector.filterMouseEvent(event, false, false);
        }
        return event;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, true);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseEvent(event, true, false);
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event)
    {
        postHumanMouse(event);
        event = VirtualMouse.filterMouseEvent(event, true);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseEvent(event, true, false);
    }

    @Override
    public MouseWheelEvent mouseWheelMoved(MouseWheelEvent event)
    {
        postHumanMouse(event);
        VirtualMouse.filterMouseEvent(event, false);
        return VirtualMouse.isSyntheticInputEvent()
            ? event : WidgetInspector.filterMouseWheelEvent(event);
    }

    @Override
    public void keyPressed(KeyEvent event)
    {
        postHumanKeyboard(event);
    }

    @Override
    public void keyReleased(KeyEvent event)
    {
        postHumanKeyboard(event);
    }

    @Override
    public void keyTyped(KeyEvent event)
    {
        postHumanKeyboard(event);
    }

    private static void postHumanMouse(MouseEvent event)
    {
        if (!VirtualMouse.isSyntheticInputEvent() && VirtualMouse.isPhysicalInputEnabled())
        {
            ScriptEventBus.getInstance().postHumanMouse(event);
        }
    }

    private static void postHumanKeyboard(KeyEvent event)
    {
        if (!Keyboard.isSyntheticInputEvent())
        {
            ScriptEventBus.getInstance().postHumanKeyboard(event);
        }
    }

    @Override
    public boolean draw(Renderable renderable, boolean drawingUi)
    {
        return true;
    }

    @Override
    public void error(String message, Throwable reason)
    {
        System.err.println(message);
        if (reason != null)
        {
            reason.printStackTrace(System.err);
        }
    }

    @Override
    public void openUrl(String url)
    {
        try
        {
            if (Desktop.isDesktopSupported())
            {
                Desktop.getDesktop().browse(URI.create(url));
            }
        }
        catch (Exception ex)
        {
            System.err.println("Unable to open " + url + ": " + ex.getMessage());
        }
    }

    @Override
    public boolean isRuneLiteClientOutdated()
    {
        return false;
    }
}
