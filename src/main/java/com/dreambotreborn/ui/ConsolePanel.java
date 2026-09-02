package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import com.dreambotreborn.api.utilities.Logger;

/** Live script console equivalent to DreamBot's console tool. */
public final class ConsolePanel extends JPanel
{
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss")
        .withZone(ZoneId.systemDefault());
    private final JTextArea text = new JTextArea();
    private final Consumer<Logger.Entry> listener = this::appendLater;

    public ConsolePanel()
    {
        super(new BorderLayout(0, 8));
        setOpaque(false);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        for (Logger.Entry entry : Logger.entries()) append(entry);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);
        JButton clear = new JButton("Clear");
        clear.setFocusable(false);
        clear.addActionListener(event -> { Logger.clear(); text.setText(""); });
        controls.add(clear);
        add(controls, BorderLayout.NORTH);
        add(new JScrollPane(text), BorderLayout.CENTER);
        Logger.subscribe(listener);
    }

    private void appendLater(Logger.Entry entry)
    {
        if (SwingUtilities.isEventDispatchThread()) append(entry);
        else SwingUtilities.invokeLater(() -> append(entry));
    }

    private void append(Logger.Entry entry)
    {
        text.append(TIME.format(entry.timestamp) + " [" + entry.type + "] " + entry.message + '\n');
        text.setCaretPosition(text.getDocument().getLength());
    }
}
