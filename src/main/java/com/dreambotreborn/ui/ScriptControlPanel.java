package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.script.AbstractScript;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.api.wrappers.interactive.Player;
import com.dreambotreborn.api.methods.interactive.Players;
import com.dreambotreborn.scripts.ScheduleManagerFrame;
import com.dreambotreborn.scripts.ScriptManagerFrame;

/** Compact live-control dashboard similar to a desktop bot client's control panel. */
public final class ScriptControlPanel extends JPanel
{
    private final ScriptManager manager;
    private final ScriptManagerFrame scripts;
    private final JLabel script = value();
    private final JLabel state = value();
    private final JLabel runtime = value();
    private final JLabel player = value();
    private final JLabel solver = value();
    private final JLabel mouse = value();
    private final JButton pause = new JButton("Pause");
    private final JButton stop = new JButton("Stop");

    public ScriptControlPanel(
        ScriptManager manager, ScriptManagerFrame scripts, ScheduleManagerFrame schedules)
    {
        super(new BorderLayout(0, 12));
        this.manager = manager;
        this.scripts = scripts;
        setOpaque(false);

        JPanel summary = new JPanel(new GridLayout(0, 2, 8, 8));
        summary.setOpaque(false);
        addRow(summary, "Script", script);
        addRow(summary, "State", state);
        addRow(summary, "Runtime", runtime);
        addRow(summary, "Player", player);
        addRow(summary, "Solver", solver);
        addRow(summary, "User input", mouse);
        summary.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ClientTheme.BORDER),
            "Session", 0, 0, null, ClientTheme.TEXT));

        JButton managerButton = new JButton("Manager");
        JButton scheduleButton = new JButton("Schedules");
        JButton start = new JButton("Start");
        managerButton.addActionListener(event -> scripts.open());
        scheduleButton.addActionListener(event -> schedules.open());
        start.addActionListener(event -> scripts.startSelectedScript());
        pause.addActionListener(event -> scripts.togglePaused());
        stop.addActionListener(event -> manager.stop());
        JPanel controls = new JPanel(new GridLayout(0, 2, 6, 6));
        controls.setOpaque(false);
        controls.add(managerButton);
        controls.add(scheduleButton);
        controls.add(start);
        controls.add(pause);
        controls.add(stop);

        add(summary, BorderLayout.NORTH);
        add(controls, BorderLayout.CENTER);
        Timer timer = new Timer(250, event -> refresh());
        timer.start();
        refresh();
    }

    private void refresh()
    {
        AbstractScript current = manager.getCurrentScript();
        script.setText(current == null ? "None" : current.getManifest().name());
        state.setText(manager.getState().name());
        runtime.setText(current == null ? "00:00:00" : current.getScriptTimer().formatTime());
        Player local = Players.getLocal();
        player.setText(local == null || local.name.isEmpty() ? "Not logged in" : local.name);
        solver.setText(manager.getRandomManager().isSolving()
            ? manager.getRandomManager().getCurrentSolver().getEventString() : "Idle");
        mouse.setText(VirtualMouse.isPhysicalInputEnabled() ? "Enabled" : "Blocked");
        boolean running = manager.isRunning();
        pause.setEnabled(running);
        pause.setText(manager.isPaused() ? "Resume" : "Pause");
        stop.setEnabled(running);
    }

    private static JLabel value()
    {
        JLabel label = new JLabel("—");
        label.setForeground(ClientTheme.MUTED);
        return label;
    }

    private static void addRow(JPanel panel, String name, JLabel value)
    {
        JLabel label = new JLabel(name);
        label.setForeground(ClientTheme.TEXT);
        panel.add(label);
        panel.add(value);
    }
}
