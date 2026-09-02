package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

/** Compact custom window chrome used by the DreamBot 3 client shell. */
final class DreamTitleBar extends JPanel
{
    private static final int HEIGHT = 28;

    private final JFrame frame;
    private final JLabel title;
    private Point dragStart;
    private Point windowStart;

    DreamTitleBar(JFrame frame, String initialTitle)
    {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(ClientTheme.TITLE_BAR);
        setPreferredSize(new Dimension(0, HEIGHT));

        JPanel caption = new JPanel(new BorderLayout(7, 0));
        caption.setOpaque(false);
        caption.setBorder(new EmptyBorder(0, 8, 0, 0));
        JLabel logo = new JLabel(Icons.client());
        title = new JLabel(initialTitle);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        caption.add(logo, BorderLayout.WEST);
        caption.add(title, BorderLayout.CENTER);
        add(caption, BorderLayout.CENTER);

        JPanel controls = new JPanel(new java.awt.GridLayout(1, 3));
        controls.setOpaque(false);
        controls.add(windowButton("\u2014", "Minimize", false, () ->
            frame.setState(JFrame.ICONIFIED)));
        JButton maximize = windowButton("\u25a1", "Fixed-size game window", false, () -> { });
        maximize.setEnabled(false);
        controls.add(maximize);
        controls.add(windowButton("\u00d7", "Close", true, () ->
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING))));
        add(controls, BorderLayout.EAST);

        MouseAdapter pressListener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent event)
            {
                dragStart = event.getLocationOnScreen();
                windowStart = frame.getLocation();
            }
        };
        MouseMotionAdapter dragListener = new MouseMotionAdapter()
        {
            @Override
            public void mouseDragged(MouseEvent event)
            {
                if (dragStart == null || windowStart == null)
                {
                    return;
                }
                Point current = event.getLocationOnScreen();
                frame.setLocation(windowStart.x + current.x - dragStart.x,
                    windowStart.y + current.y - dragStart.y);
            }
        };
        addMouseListener(pressListener);
        addMouseMotionListener(dragListener);
        caption.addMouseListener(pressListener);
        caption.addMouseMotionListener(dragListener);
        title.addMouseListener(pressListener);
        title.addMouseMotionListener(dragListener);
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    void setTitle(String value)
    {
        title.setText(value);
        frame.setTitle(value);
    }

    private JButton windowButton(String text, String tooltip, boolean close, Runnable action)
    {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(42, HEIGHT));
        button.setForeground(new Color(215, 215, 215));
        button.setBackground(ClientTheme.TITLE_BAR);
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 15f));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(true);
        button.addActionListener(event -> action.run());
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent event)
            {
                if (button.isEnabled())
                {
                    button.setBackground(close ? new Color(196, 43, 28) : ClientTheme.HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event)
            {
                button.setBackground(ClientTheme.TITLE_BAR);
            }
        });
        return button;
    }
}
