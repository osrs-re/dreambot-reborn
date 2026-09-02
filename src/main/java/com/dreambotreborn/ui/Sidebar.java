package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

/** RuneLite-style tab strip with a collapsible content pane. */
public final class Sidebar extends JPanel
{
    private static final int STRIP_WIDTH = 42;
    private static final int PANE_WIDTH = 280;
    private static final Color PANE_BACKGROUND = ClientTheme.PANEL;

    private final Runnable layoutChanged;
    private final JPanel buttons = new JPanel();
    private final JPanel pane = new JPanel();
    private final CardLayout cards = new CardLayout();
    private final Map<String, Tab> tabs = new LinkedHashMap<>();
    private String selectedTab;

    Sidebar(Runnable layoutChanged)
    {
        super(new BorderLayout());
        this.layoutChanged = Objects.requireNonNull(layoutChanged, "layoutChanged");
        setBackground(BottomBar.BACKGROUND);

        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.setBackground(BottomBar.BACKGROUND);

        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(BottomBar.BACKGROUND);
        strip.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BottomBar.BORDER));
        strip.setPreferredSize(new Dimension(STRIP_WIDTH, 0));
        strip.add(buttons, BorderLayout.NORTH);

        pane.setLayout(cards);
        pane.setBackground(PANE_BACKGROUND);
        pane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, BottomBar.BORDER));
        pane.setPreferredSize(new Dimension(PANE_WIDTH, 0));
        pane.setVisible(false);

        add(pane, BorderLayout.CENTER);
        add(strip, BorderLayout.EAST);
        setVisible(false);
    }

    public JButton addTab(String id, String title, Icon icon, JComponent content)
    {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(content, "content");
        if (tabs.containsKey(id))
        {
            throw new IllegalArgumentException("A sidebar tab named '" + id + "' already exists");
        }

        JButton button = createTabButton(title, icon);
        JPanel card = createCard(title, content);
        Tab tab = new Tab(button, card);
        tabs.put(id, tab);
        pane.add(card, id);
        buttons.add(button);
        button.addActionListener(event -> toggle(id));
        buttons.revalidate();
        buttons.repaint();
        return button;
    }

    public void show(String id)
    {
        Tab tab = tabs.get(id);
        if (tab == null)
        {
            throw new IllegalArgumentException("Unknown sidebar tab '" + id + "'");
        }

        selectedTab = id;
        cards.show(pane, id);
        pane.setVisible(true);
        setVisible(true);
        updateButtons();
        revalidate();
        layoutChanged.run();
    }

    public void close()
    {
        selectedTab = null;
        pane.setVisible(false);
        setVisible(false);
        updateButtons();
        revalidate();
        layoutChanged.run();
    }

    public boolean isOpen()
    {
        return selectedTab != null;
    }

    public String getSelectedTab()
    {
        return selectedTab;
    }

    private void toggle(String id)
    {
        if (id.equals(selectedTab))
        {
            close();
        }
        else
        {
            show(id);
        }
    }

    private void updateButtons()
    {
        for (Map.Entry<String, Tab> entry : tabs.entrySet())
        {
            boolean selected = entry.getKey().equals(selectedTab);
            entry.getValue().button.setBackground(
                selected ? BottomBar.BUTTON_SELECTED : BottomBar.BACKGROUND);
        }
    }

    private JButton createTabButton(String title, Icon icon)
    {
        JButton button = new JButton(icon);
        button.setUI(new BasicButtonUI());
        button.setToolTipText(title);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(STRIP_WIDTH, STRIP_WIDTH));
        button.setMinimumSize(new Dimension(STRIP_WIDTH, STRIP_WIDTH));
        button.setMaximumSize(new Dimension(STRIP_WIDTH, STRIP_WIDTH));
        button.setBackground(BottomBar.BACKGROUND);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setOpaque(true);
        return button;
    }

    private JPanel createCard(String title, JComponent content)
    {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANE_BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BottomBar.BACKGROUND);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BottomBar.BORDER));
        header.setPreferredSize(new Dimension(PANE_WIDTH, 42));

        JLabel label = new JLabel(title);
        label.setForeground(new Color(225, 227, 232));
        label.setBorder(new EmptyBorder(0, 12, 0, 0));
        header.add(label, BorderLayout.CENTER);

        JButton close = createTabButton("Close", Icons.close());
        close.setPreferredSize(new Dimension(38, 41));
        close.setMinimumSize(new Dimension(38, 41));
        close.setMaximumSize(new Dimension(38, 41));
        close.setHorizontalAlignment(SwingConstants.CENTER);
        close.addActionListener(event -> close());
        header.add(close, BorderLayout.EAST);

        content.setBorder(BorderFactory.createCompoundBorder(
            content.getBorder(), new EmptyBorder(12, 12, 12, 12)));
        card.add(header, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private static final class Tab
    {
        private final JButton button;
        @SuppressWarnings("unused")
        private final JPanel card;

        private Tab(JButton button, JPanel card)
        {
            this.button = button;
            this.card = card;
        }
    }
}
