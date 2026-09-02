package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import com.dreambotreborn.api.randoms.BreakSolver;
import com.dreambotreborn.api.script.ScriptManager;

/** Manual break manager backed by the script runtime's priority solver. */
public final class BreakPanel extends JPanel
{
    private final BreakSolver solver;
    private final JSpinner minutes = new JSpinner(new SpinnerNumberModel(5, 1, 240, 1));
    private final JCheckBox enabled = new JCheckBox("Enable break solver", true);
    private final JLabel status = new JLabel("No break scheduled");

    public BreakPanel(ScriptManager manager)
    {
        super(new BorderLayout(0, 10));
        solver = manager.getRandomManager().getBreakSolver();
        setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ClientTheme.BORDER),
            "Breaks", 0, 0, null, ClientTheme.TEXT));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(4, 4, 8, 4);
        enabled.setOpaque(false);
        form.add(enabled, c);
        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel("Duration (minutes)"), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(minutes, c);

        JButton start = new JButton("Start break");
        JButton cancel = new JButton("Cancel");
        start.addActionListener(event ->
            solver.schedule(Duration.ofMinutes(((Number) minutes.getValue()).longValue())));
        cancel.addActionListener(event -> solver.cancel());
        enabled.addActionListener(event ->
        {
            if (enabled.isSelected()) solver.enable();
            else { solver.disable(); solver.cancel(); }
        });
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        controls.setOpaque(false);
        controls.add(start);
        controls.add(cancel);
        status.setForeground(ClientTheme.MUTED);

        add(form, BorderLayout.NORTH);
        add(controls, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        new Timer(250, event -> refresh()).start();
    }

    private void refresh()
    {
        long remaining = solver.getRemainingMillis();
        if (remaining <= 0L) status.setText("No break scheduled");
        else status.setText(String.format("Break active • %02d:%02d remaining",
            remaining / 60_000L, (remaining / 1_000L) % 60L));
    }
}
