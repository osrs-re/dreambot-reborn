package com.dreambotreborn.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/** Toolbar displayed directly below the game canvas. */
public final class BottomBar extends javax.swing.JPanel
{
    static final Color BACKGROUND = ClientTheme.BACKGROUND;
    static final Color BUTTON = ClientTheme.CONTROL;
    static final Color BUTTON_SELECTED = ClientTheme.SELECTED;
    static final Color BORDER = ClientTheme.BORDER;

    private final javax.swing.JPanel controls = new javax.swing.JPanel(
        new FlowLayout(FlowLayout.RIGHT, 1, 1));
    private final JLabel status = new JLabel("Ready");

    BottomBar()
    {
        super(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        setPreferredSize(new Dimension(0, 31));
        controls.setOpaque(false);
        controls.setBorder(new EmptyBorder(0, 0, 0, 4));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(0, 7, 0, 4));
        JLabel statusIcon = new JLabel(Icons.status());
        statusIcon.setPreferredSize(new Dimension(15, 29));
        status.setForeground(ClientTheme.TEXT);
        status.setFont(status.getFont().deriveFont(11f));
        statusPanel.add(statusIcon);
        statusPanel.add(status);
        add(statusPanel, BorderLayout.CENTER);
        add(controls, BorderLayout.EAST);
    }

    public JButton addButton(String tooltip, Icon icon, Runnable action)
    {
        Objects.requireNonNull(action, "action");
        JButton button = style(new JButton(icon), tooltip);
        button.addActionListener(event -> action.run());
        controls.add(button);
        revalidate();
        repaint();
        return button;
    }

    public JToggleButton addToggleButton(
        String tooltip,
        Icon unselectedIcon,
        Icon selectedIcon,
        Consumer<Boolean> action)
    {
        Objects.requireNonNull(action, "action");
        JToggleButton button = style(new JToggleButton(unselectedIcon), tooltip);
        button.addActionListener(event ->
        {
            button.setIcon(button.isSelected() ? selectedIcon : unselectedIcon);
            button.setBackground(button.isSelected() ? BUTTON_SELECTED : BUTTON);
            action.accept(button.isSelected());
        });
        controls.add(button);
        revalidate();
        repaint();
        return button;
    }

    public void addSeparator(int index)
    {
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(BORDER);
        separator.setPreferredSize(new Dimension(1, 21));
        controls.add(separator, Math.max(0, Math.min(index, controls.getComponentCount())));
    }

    public void moveTo(Component component, int index)
    {
        if (component != null && component.getParent() == controls)
        {
            controls.setComponentZOrder(component,
                Math.max(0, Math.min(index, controls.getComponentCount() - 1)));
            controls.revalidate();
            controls.repaint();
        }
    }

    public void setStatus(String value)
    {
        status.setText(value == null ? "" : value);
    }

    private static <T extends javax.swing.AbstractButton> T style(T button, String tooltip)
    {
        button.setUI(new BasicButtonUI());
        button.setToolTipText(tooltip);
        button.setPreferredSize(new Dimension(28, 28));
        button.setMinimumSize(new Dimension(28, 28));
        button.setMaximumSize(new Dimension(28, 28));
        button.setBackground(BUTTON);
        button.setBorder(new EmptyBorder(5, 5, 5, 5));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(true);
        button.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent event)
            {
                if (button.isEnabled() && !button.isSelected())
                {
                    button.setBackground(ClientTheme.HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event)
            {
                button.setBackground(button.isSelected() ? BUTTON_SELECTED : BUTTON);
            }
        });
        return button;
    }
}
