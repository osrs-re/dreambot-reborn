package com.dreambotreborn;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import com.dreambotreborn.api.DreamBotRebornApi;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.input.Keyboard;
import com.dreambotreborn.api.internal.ClientThread;
import com.dreambotreborn.api.input.keyboard.KeyboardProfile;
import com.dreambotreborn.api.input.mouse.algorithm.MouseProfile;
import com.dreambotreborn.api.methods.interactive.GameObjects;
import com.dreambotreborn.accounts.AccountLogin;
import com.dreambotreborn.accounts.AccountManagerFrame;
import com.dreambotreborn.devtools.WidgetInspector;
import com.dreambotreborn.devtools.WidgetInspectorPanel;
import com.dreambotreborn.settings.ClientSettings;
import com.dreambotreborn.settings.PreferredWorld;
import com.dreambotreborn.settings.SettingsPanel;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.scripts.ScriptManagerFrame;
import com.dreambotreborn.scripts.ScheduleManagerFrame;
import com.dreambotreborn.ui.ClientUi;
import com.dreambotreborn.ui.ClientWindow;
import com.dreambotreborn.ui.Icons;
import com.dreambotreborn.ui.ClientTheme;
import com.dreambotreborn.ui.ConsolePanel;
import com.dreambotreborn.ui.ClientMenuBar;
import com.dreambotreborn.ui.ScriptControlPanel;
import com.dreambotreborn.ui.BreakPanel;
import com.dreambotreborn.api.script.schedule.ScriptScheduler;
import net.runelite.api.Client;
import net.runelite.api.hooks.Callbacks;

public final class DreamBotReborn
{
    private static final URI JAV_CONFIG =
        URI.create("https://oldschool.config.runescape.com/jav_config.ws");

    private DreamBotReborn()
    {
    }

    public static void main(String[] args) throws Exception
    {
        System.setProperty("apple.awt.application.name", "DreamBot Reborn");
        System.setProperty("jagex.disableBouncyCastle", "true");
        ClientTheme.install();

        Path dataDirectory = Paths.get(System.getProperty("user.home"), ".dreambot-reborn");
        Files.createDirectories(dataDirectory);
        System.setProperty("jagex.userhome", dataDirectory.toString());

        Path settingsPath = dataDirectory.resolve("settings.json");
        ClientSettings settings;
        try
        {
            settings = ClientSettings.loadOrCreate(settingsPath);
        }
        catch (Exception ex)
        {
            System.err.println("Unable to load settings.json; using defaults: " + ex.getMessage());
            settings = ClientSettings.defaults(settingsPath);
        }

        applyInputSettings(settings);

        JavConfig config = JavConfig.fetch(JAV_CONFIG)
            .withPreferredWorld(settings.getPreferredWorld());
        Client client = createClient(config);
        Component game = (Component) client;
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable ->
        {
            Thread thread = new Thread(runnable, "DreamBot Reborn Worker");
            thread.setDaemon(true);
            return thread;
        });

        DreamBotRebornApi.initialize(client);
        VirtualMouse.initialize(client, game);
        Keyboard.initialize(client, game);
        AccountLogin.initialize(client);
        PreferredWorld.initialize(client);
        WidgetInspector.initialize(client);
        ScriptManager.getScriptManager().initialize(dataDirectory.resolve("scripts"));
        injectRequiredMembers(client, new MinimalCallbacks(game), executor, dataDirectory);
        showWindow(client, game, executor, dataDirectory, settings);
        client.initialize();
    }

    private static void applyInputSettings(ClientSettings settings)
    {
        MouseProfile.setSpeed(settings.getMouseSpeed());
        MouseProfile.setOvershootEnabled(settings.isMouseOvershoot());
        MouseProfile.setMouseTiming(settings::getClickHoldMillis);
        KeyboardProfile.setWordsPerMinute(settings.getKeyboardWpm());
        KeyboardProfile.setMakeMistakes(settings.isKeyboardMistakes());
        VirtualMouse.setTrailDuration(settings.getMouseTrailMillis());
    }

    private static Client createClient(JavConfig config) throws Exception
    {
        Class<?> clientClass = Class.forName(config.getInitialClass());
        Object instance = clientClass.getDeclaredConstructor().newInstance();
        if (!(instance instanceof Client) || !(instance instanceof Component))
        {
            throw new IllegalStateException(
                "The configured client is not an injected RuneLite AWT client: " + clientClass.getName());
        }

        Client client = (Client) instance;
        client.setConfiguration(config);
        return client;
    }

    private static void injectRequiredMembers(
        Client client,
        Callbacks callbacks,
        ScheduledExecutorService executor,
        Path dataDirectory) throws IllegalAccessException, InvocationTargetException
    {
        for (Field field : client.getClass().getDeclaredFields())
        {
            if (field.getAnnotation(Inject.class) == null)
            {
                continue;
            }

            Object value;
            if (field.getType() == Callbacks.class)
            {
                value = callbacks;
            }
            else if (field.getType() == ScheduledExecutorService.class)
            {
                value = executor;
            }
            else if (field.getType() == java.io.File.class)
            {
                value = dataDirectory.toFile();
            }
            else if (field.getType() == boolean.class)
            {
                value = false;
            }
            else
            {
                throw new IllegalStateException("Unsupported injected field: " + field);
            }

            field.setAccessible(true);
            field.set(client, value);
        }

        for (Method method : client.getClass().getDeclaredMethods())
        {
            if (method.getAnnotation(Inject.class) != null)
            {
                if (method.getParameterCount() != 0)
                {
                    throw new IllegalStateException("Unsupported injected method: " + method);
                }
                method.setAccessible(true);
                method.invoke(client);
            }
        }
    }

    private static void showWindow(
        Client client,
        Component game,
        ScheduledExecutorService executor,
        Path dataDirectory,
        ClientSettings settings) throws Exception
    {
        SwingUtilities.invokeAndWait(() ->
        {
            JLabel objectCount = new JLabel("Objects in scene: 0");
            objectCount.setForeground(new Color(210, 214, 220));

            JPanel objectsPane = new JPanel();
            objectsPane.setOpaque(false);
            objectsPane.setLayout(new BoxLayout(objectsPane, BoxLayout.Y_AXIS));
            objectsPane.add(objectCount);

            ClientWindow window = new ClientWindow(
                "DreamBot Reborn 1.0 - Now loading...", game, () ->
            {
                ScriptManager.getScriptManager().shutdown();
                ScriptScheduler.getScriptScheduler().reset();
                ClientThread.shutdown();
                client.stopNow();
                executor.shutdownNow();
                ClientUi.window().frame().dispose();
                System.exit(0);
            });
            ClientUi.install(window);

            javax.swing.JToggleButton mouseInput = window.bottomBar().addToggleButton(
                "Physical mouse input",
                Icons.mouseOff(),
                Icons.mouse(),
                VirtualMouse::setPhysicalInputEnabled);
            mouseInput.doClick();
            AccountManagerFrame accountManager = new AccountManagerFrame(
                window.frame(), dataDirectory.resolve("accounts.json"));
            ScriptManagerFrame scriptManager = new ScriptManagerFrame(
                window.frame(), ScriptManager.getScriptManager(), accountManager, mouseInput);
            ScheduleManagerFrame scheduleManager = new ScheduleManagerFrame(
                window.frame(), ScriptManager.getScriptManager(), accountManager);
            javax.swing.JButton scriptButton = window.bottomBar().addButton(
                "Script manager", Icons.scripts(), scriptManager::open);
            javax.swing.JButton accountButton = window.bottomBar().addButton(
                "Accounts", Icons.accounts(), accountManager::open);
            javax.swing.JButton playButton = window.bottomBar().addButton(
                "Start selected script", Icons.play(), scriptManager::startSelectedScript);
            javax.swing.JButton pauseButton = window.bottomBar().addButton(
                "Pause or resume script", Icons.pause(), scriptManager::togglePaused);
            javax.swing.JButton stopButton = window.bottomBar().addButton(
                "Stop script", Icons.stop(), ScriptManager.getScriptManager()::stop);
            javax.swing.JButton reloadButton = window.bottomBar().addButton(
                "Reload scripts", Icons.reload(), scriptManager::reload);
            javax.swing.JButton scheduleButton = window.bottomBar().addButton(
                "Script schedules", Icons.schedule(), scheduleManager::open);
            javax.swing.JButton settingsButton = window.bottomBar().addButton(
                "Settings", Icons.settings(), () -> window.sidebar().show("settings"));
            javax.swing.JButton toolsButton = window.bottomBar().addButton(
                "Developer tools", Icons.tools(), () -> window.sidebar().show("developer"));
            window.bottomBar().moveTo(reloadButton, 0);
            window.bottomBar().moveTo(playButton, 1);
            window.bottomBar().moveTo(pauseButton, 2);
            window.bottomBar().moveTo(stopButton, 3);
            window.bottomBar().moveTo(scriptButton, 4);
            window.bottomBar().moveTo(accountButton, 5);
            window.bottomBar().moveTo(scheduleButton, 6);
            window.bottomBar().moveTo(mouseInput, 7);
            window.bottomBar().moveTo(settingsButton, 8);
            window.bottomBar().moveTo(toolsButton, 9);
            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);
            window.installMenuBar(ClientMenuBar.create(
                window.frame(), window.sidebar(), scriptManager, scheduleManager,
                accountManager, mouseInput));
            window.sidebar().addTab("scripts", "Script Control", Icons.scripts(),
                new ScriptControlPanel(
                    ScriptManager.getScriptManager(), scriptManager, scheduleManager));
            window.sidebar().addTab("breaks", "Break Manager", Icons.breaks(),
                new BreakPanel(ScriptManager.getScriptManager()));
            window.sidebar().addTab("objects", "Objects", Icons.objects(), objectsPane);
            window.sidebar().addTab(
                "developer", "Developer", Icons.developer(), new WidgetInspectorPanel());
            window.sidebar().addTab(
                "console", "Console", Icons.console(), new ConsolePanel());
            window.sidebar().addTab(
                "settings", "Settings", Icons.settings(), new SettingsPanel(settings));

            Timer objectCountTimer = new Timer(500,
                event ->
                {
                    ScriptManager manager = ScriptManager.getScriptManager();
                    objectCount.setText("Objects in scene: " + GameObjects.all().count());
                    pauseButton.setEnabled(manager.isRunning());
                    stopButton.setEnabled(manager.isRunning());
                    String running = manager.getCurrentScript() == null ? "No script"
                        : manager.getCurrentScript().getManifest().name() + " • " + manager.getState();
                    String solver = manager.getRandomManager().isSolving()
                        ? " • " + manager.getRandomManager().getCurrentSolver().getEventString() : "";
                    window.bottomBar().setStatus(running + solver + " • Mouse "
                        + (VirtualMouse.isPhysicalInputEnabled() ? "enabled" : "blocked"));
                    window.setClientTitle("DreamBot Reborn 1.0 - "
                        + (manager.getCurrentScript() == null
                            ? readableGameState(client.getGameState().toString())
                            : manager.getCurrentScript().getManifest().name()));
                });
            objectCountTimer.start();
            window.show();
        });
    }

    private static String readableGameState(String state)
    {
        if ("STARTING".equals(state) || "LOADING".equals(state)
            || "HOPPING".equals(state) || "UNKNOWN".equals(state))
        {
            return "Now loading...";
        }
        StringBuilder text = new StringBuilder();
        for (String word : state.toLowerCase(java.util.Locale.ROOT).split("_"))
        {
            if (text.length() > 0) text.append(' ');
            if (!word.isEmpty())
                text.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return text.toString();
    }
}
