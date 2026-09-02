package com.dreambotreborn.settings;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.api.input.keyboard.KeyboardProfile;
import com.dreambotreborn.api.input.mouse.algorithm.MouseProfile;
import com.dreambotreborn.ui.ClientTheme;

/** Live mouse/keyboard profile editor; every change is persisted immediately. */
public final class InputSettingsPanel extends JPanel
{
    private final ClientSettings settings;
    private final JLabel status = new JLabel(" ");

    public InputSettingsPanel(ClientSettings settings)
    {
        super(new GridBagLayout());
        this.settings = settings;
        setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(3, 0, 7, 8);

        JSpinner speed = spinner(settings.getMouseSpeed(), 100.0, 2500.0, 50.0);
        addRow("Mouse speed", speed, constraints);
        JSpinner click = spinner(settings.getClickHoldMillis(), 1, 500, 5);
        addRow("Click hold (ms)", click, constraints);
        JSpinner trail = spinner(settings.getMouseTrailMillis(), 0L, 10_000L, 50L);
        addRow("Trail duration (ms)", trail, constraints);
        JSpinner wpm = spinner(settings.getKeyboardWpm(), 10.0, 300.0, 5.0);
        addRow("Keyboard WPM", wpm, constraints);

        JCheckBox overshoot = new JCheckBox("Allow natural overshoot", settings.isMouseOvershoot());
        overshoot.setOpaque(false);
        addWide(overshoot, constraints);
        JCheckBox mistakes = new JCheckBox("Occasional typing corrections", settings.isKeyboardMistakes());
        mistakes.setOpaque(false);
        addWide(mistakes, constraints);
        status.setForeground(ClientTheme.MUTED);
        addWide(status, constraints);

        speed.addChangeListener(event -> save(() ->
        {
            double value = ((Number) speed.getValue()).doubleValue();
            settings.setMouseSpeed(value);
            MouseProfile.setSpeed(value);
        }));
        click.addChangeListener(event -> save(() ->
        {
            int value = ((Number) click.getValue()).intValue();
            settings.setClickHoldMillis(value);
            MouseProfile.setMouseTiming(settings::getClickHoldMillis);
        }));
        trail.addChangeListener(event -> save(() ->
        {
            long value = ((Number) trail.getValue()).longValue();
            settings.setMouseTrailMillis(value);
            VirtualMouse.setTrailDuration(value);
        }));
        wpm.addChangeListener(event -> save(() ->
        {
            double value = ((Number) wpm.getValue()).doubleValue();
            settings.setKeyboardWpm(value);
            KeyboardProfile.setWordsPerMinute(value);
        }));
        overshoot.addActionListener(event -> save(() ->
        {
            settings.setMouseOvershoot(overshoot.isSelected());
            MouseProfile.setOvershootEnabled(overshoot.isSelected());
        }));
        mistakes.addActionListener(event -> save(() ->
        {
            settings.setKeyboardMistakes(mistakes.isSelected());
            KeyboardProfile.setMakeMistakes(mistakes.isSelected());
        }));
    }

    private void addRow(String label, java.awt.Component component, GridBagConstraints constraints)
    {
        constraints.gridx = 0;
        constraints.weightx = 1;
        add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 0;
        add(component, constraints);
        constraints.gridy++;
    }

    private void addWide(java.awt.Component component, GridBagConstraints constraints)
    {
        constraints.gridx = 0;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        add(component, constraints);
        constraints.gridwidth = 1;
        constraints.gridy++;
    }

    private static JSpinner spinner(Number value, Comparable<?> minimum, Comparable<?> maximum, Number step)
    {
        return new JSpinner(new SpinnerNumberModel(value, minimum, maximum, step));
    }

    private void save(IoAction action)
    {
        try { action.run(); status.setText("Saved"); }
        catch (IOException | IllegalArgumentException error) { status.setText("Save failed: " + error.getMessage()); }
    }

    @FunctionalInterface
    private interface IoAction { void run() throws IOException; }
}
