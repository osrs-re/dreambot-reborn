package com.dreambotreborn.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import net.runelite.api.Constants;

/** Main Swing window containing the game canvas and extension toolbars. */
public final class ClientWindow
{
    private final JFrame frame;
    private final BottomBar bottomBar;
    private final Sidebar sidebar;
    private final DreamTitleBar titleBar;
    private final JPanel menuHost = new JPanel(new BorderLayout());

    public ClientWindow(String title, Component game, Runnable closeAction)
    {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(closeAction, "closeAction");

        frame = new JFrame(title);
        frame.setUndecorated(true);
        bottomBar = new BottomBar();
        sidebar = new Sidebar(this::repack);
        titleBar = new DreamTitleBar(frame, title);

        JPanel gameHost = new JPanel(new BorderLayout());
        gameHost.setBackground(Color.BLACK);
        gameHost.setPreferredSize(Constants.GAME_FIXED_SIZE);
        gameHost.add(game, BorderLayout.CENTER);

        JPanel canvasRow = new JPanel(new BorderLayout());
        canvasRow.setBackground(Color.BLACK);
        canvasRow.add(gameHost, BorderLayout.CENTER);
        canvasRow.add(sidebar, BorderLayout.EAST);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BottomBar.BACKGROUND);
        root.setBorder(BorderFactory.createLineBorder(ClientTheme.BORDER));

        JPanel chrome = new JPanel(new BorderLayout());
        chrome.setBackground(ClientTheme.MENU_BAR);
        chrome.add(titleBar, BorderLayout.NORTH);
        chrome.add(menuHost, BorderLayout.SOUTH);
        root.add(chrome, BorderLayout.NORTH);
        root.add(canvasRow, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setContentPane(root);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                closeAction.run();
            }
        });
    }

    public void installMenuBar(JMenuBar menuBar)
    {
        menuHost.removeAll();
        menuHost.add(menuBar, BorderLayout.CENTER);
        menuHost.revalidate();
        menuHost.repaint();
    }

    public void setClientTitle(String title)
    {
        titleBar.setTitle(title);
    }

    public BottomBar bottomBar()
    {
        return bottomBar;
    }

    public Sidebar sidebar()
    {
        return sidebar;
    }

    public JFrame frame()
    {
        return frame;
    }

    public void show()
    {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void repack()
    {
        Point location = frame.isShowing() ? frame.getLocation() : null;
        frame.pack();
        if (location != null)
        {
            frame.setLocation(location);
        }
    }
}
