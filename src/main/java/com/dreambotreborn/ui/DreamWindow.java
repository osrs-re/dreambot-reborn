package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/** Applies the same compact client chrome to DreamBot Reborn's secondary tool windows. */
public final class DreamWindow
{
    private DreamWindow()
    {
    }

    public static void decorate(JFrame frame, JComponent content)
    {
        frame.setUndecorated(true);
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(ClientTheme.BACKGROUND);
        shell.setBorder(BorderFactory.createLineBorder(ClientTheme.BORDER));
        shell.add(new DreamTitleBar(frame, frame.getTitle()), BorderLayout.NORTH);
        shell.add(content, BorderLayout.CENTER);
        frame.setContentPane(shell);
    }
}
