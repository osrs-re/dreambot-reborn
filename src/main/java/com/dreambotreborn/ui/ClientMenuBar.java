package com.dreambotreborn.ui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.accounts.AccountManagerFrame;
import com.dreambotreborn.scripts.ScheduleManagerFrame;
import com.dreambotreborn.scripts.ScriptManagerFrame;

/** Desktop menu exposing all major client tools and keyboard shortcuts. */
public final class ClientMenuBar
{
    private ClientMenuBar()
    {
    }

    public static JMenuBar create(
        JFrame frame,
        Sidebar sidebar,
        ScriptManagerFrame scripts,
        ScheduleManagerFrame schedules,
        AccountManagerFrame accounts,
        JToggleButton physicalMouse)
    {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(ClientTheme.MENU_BAR);
        bar.setBorder(javax.swing.BorderFactory.createMatteBorder(
            0, 0, 1, 0, ClientTheme.BORDER));
        bar.setPreferredSize(new java.awt.Dimension(0, 27));
        add(bar, toolsMenu(sidebar, scripts, schedules));
        add(bar, clientMenu(frame, sidebar, accounts, physicalMouse));
        add(bar, entitiesMenu(sidebar));
        add(bar, miscellaneousMenu(sidebar, scripts));
        add(bar, helpMenu(frame));
        return bar;
    }

    private static void add(JMenuBar bar, JMenu menu)
    {
        menu.setOpaque(true);
        menu.setBackground(ClientTheme.MENU_BAR);
        menu.setBorder(new EmptyBorder(0, 10, 0, 10));
        menu.setFont(menu.getFont().deriveFont(12f));
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
        bar.add(menu);
    }

    private static JMenu clientMenu(
        JFrame frame, Sidebar sidebar, AccountManagerFrame accounts, JToggleButton physicalMouse)
    {
        JMenu menu = new JMenu("Client");
        menu.setMnemonic(KeyEvent.VK_C);
        menu.add(item("Accounts…", KeyEvent.VK_A,
            KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), accounts::open));
        menu.add(item("Settings", KeyEvent.VK_S, null, () -> sidebar.show("settings")));
        menu.addSeparator();
        JCheckBoxMenuItem input = new JCheckBoxMenuItem("Physical mouse input");
        input.setSelected(physicalMouse.isSelected());
        input.addActionListener(event ->
        {
            if (physicalMouse.isSelected() != input.isSelected()) physicalMouse.doClick();
        });
        physicalMouse.addChangeListener(event ->
        {
            if (input.isSelected() != physicalMouse.isSelected())
                input.setSelected(physicalMouse.isSelected());
        });
        menu.add(input);
        menu.addSeparator();
        menu.add(item("Exit", KeyEvent.VK_X,
            KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK),
            () -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING))));
        return menu;
    }

    private static JMenu toolsMenu(
        Sidebar sidebar, ScriptManagerFrame scripts, ScheduleManagerFrame schedules)
    {
        JMenu menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.add(item("Script manager…", KeyEvent.VK_M,
            KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), scripts::open));
        menu.add(item("Schedule manager…", KeyEvent.VK_H, null, schedules::open));
        menu.add(item("Break manager", KeyEvent.VK_B, null, () -> sidebar.show("breaks")));
        menu.addSeparator();
        menu.add(item("Start selected", KeyEvent.VK_R,
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), scripts::startSelectedScript));
        menu.add(item("Pause / resume", KeyEvent.VK_P,
            KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), scripts::togglePaused));
        menu.add(item("Stop", KeyEvent.VK_T,
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0), ScriptManager.getScriptManager()::stop));
        menu.add(item("Reload scripts", KeyEvent.VK_E,
            KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), scripts::reload));
        return menu;
    }

    private static JMenu entitiesMenu(Sidebar sidebar)
    {
        JMenu menu = new JMenu("Entities");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.add(item("Widgets", KeyEvent.VK_W,
            KeyStroke.getKeyStroke(KeyEvent.VK_D,
                InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
            () -> sidebar.show("developer")));
        menu.add(item("Game objects", KeyEvent.VK_O, null,
            () -> sidebar.show("objects")));
        return menu;
    }

    private static JMenu miscellaneousMenu(Sidebar sidebar, ScriptManagerFrame scripts)
    {
        JMenu menu = new JMenu("Miscellaneous");
        menu.setMnemonic(KeyEvent.VK_M);
        menu.add(item("Console", KeyEvent.VK_C,
            KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK),
            () -> sidebar.show("console")));
        menu.addSeparator();
        menu.add(item("Reload scripts", KeyEvent.VK_R, null, scripts::reload));
        return menu;
    }

    private static JMenu helpMenu(JFrame frame)
    {
        JMenu menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.add(item("About DreamBot Reborn", KeyEvent.VK_A, null, () ->
            JOptionPane.showMessageDialog(frame,
                "DreamBot Reborn\nAutomation API and script runtime\n\n"
                    + "DreamBot-inspired workflow, built on RuneLite's public API.",
                "About", JOptionPane.INFORMATION_MESSAGE)));
        return menu;
    }

    private static JMenuItem item(String text, int mnemonic, KeyStroke accelerator, Runnable action)
    {
        JMenuItem item = new JMenuItem(text);
        item.setMnemonic(mnemonic);
        if (accelerator != null) item.setAccelerator(accelerator);
        item.addActionListener(event -> action.run());
        return item;
    }
}
