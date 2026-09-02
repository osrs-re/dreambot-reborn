package com.dreambotreborn.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;
import com.dreambotreborn.ui.ClientTheme;

/** RuneLite-style settings content with immediate JSON persistence. */
public final class SettingsPanel extends JPanel
{
    private static final Color BACKGROUND = ClientTheme.BACKGROUND;
    private static final Color FOREGROUND = ClientTheme.TEXT;
    private static final Color MUTED = ClientTheme.MUTED;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ClientSettings settings;
    private final JCheckBox enabled = new JCheckBox("Use preferred world");
    private final JSpinner world = new JSpinner(new SpinnerNumberModel(301, 301, 999, 1));
    private final JLabel status = new JLabel();
    private boolean loading;

    public SettingsPanel(ClientSettings settings)
    {
        super(new BorderLayout(0, 10));
        this.settings = settings;
        setBackground(BACKGROUND);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 0, 10, 0);

        enabled.setOpaque(false);
        enabled.setForeground(FOREGROUND);
        enabled.setFocusable(false);
        form.add(enabled, constraints);

        constraints.gridy++;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.insets = new Insets(2, 0, 8, 8);
        JLabel worldLabel = new JLabel("World:");
        worldLabel.setForeground(FOREGROUND);
        form.add(worldLabel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.insets = new Insets(2, 0, 8, 0);
        form.add(world, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.insets = new Insets(4, 0, 4, 0);
        JLabel explanation = new JLabel(
            "<html>Saved immediately. Applied before the game client initializes on the next start.</html>");
        explanation.setForeground(MUTED);
        form.add(explanation, constraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        actions.setOpaque(false);
        JButton apply = new JButton("Apply now");
        apply.setFocusable(false);
        apply.addActionListener(event -> applyNow());
        actions.add(apply);
        constraints.gridy++;
        form.add(actions, constraints);

        status.setForeground(MUTED);
        status.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        form.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(com.dreambotreborn.ui.ClientTheme.BORDER),
            "Client", 0, 0, null, FOREGROUND));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(form);
        InputSettingsPanel inputSettings = new InputSettingsPanel(settings);
        inputSettings.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(com.dreambotreborn.ui.ClientTheme.BORDER),
            "Input profile", 0, 0, null, FOREGROUND));
        content.add(inputSettings);
        add(content, BorderLayout.NORTH);
        add(status, BorderLayout.SOUTH);

        loading = true;
        int preferred = settings.getPreferredWorld();
        enabled.setSelected(preferred != 0);
        world.setValue(preferred == 0 ? 301 : preferred);
        world.setEnabled(enabled.isSelected());
        loading = false;

        enabled.addActionListener(event ->
        {
            world.setEnabled(enabled.isSelected());
            save();
        });
        world.addChangeListener(event -> save());
        status.setText("JSON: " + settings.getPath());
    }

    private void save()
    {
        if (loading)
        {
            return;
        }
        int value = enabled.isSelected() ? (Integer) world.getValue() : 0;
        try
        {
            settings.setPreferredWorld(value);
            status.setText("Saved " + LocalTime.now().format(TIME));
        }
        catch (IOException ex)
        {
            status.setText("Save failed: " + ex.getMessage());
        }
    }

    private void applyNow()
    {
        if (!enabled.isSelected())
        {
            status.setText("Enable preferred world first");
            return;
        }
        int value = (Integer) world.getValue();
        status.setText("Selecting world " + value + "…");
        PreferredWorld.apply(value).thenAccept(message -> SwingUtilities.invokeLater(() ->
            status.setText(message)));
    }
}
