package com.dreambotreborn.ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/** Shared palette matching the compact DreamBot 3 desktop client. */
public final class ClientTheme
{
    public static final Color BACKGROUND = new Color(37, 37, 37);
    public static final Color PANEL = new Color(45, 45, 45);
    public static final Color PANEL_DARK = new Color(31, 31, 31);
    public static final Color TITLE_BAR = new Color(28, 28, 28);
    public static final Color MENU_BAR = new Color(43, 43, 43);
    public static final Color CONTROL = new Color(43, 43, 43);
    public static final Color HOVER = new Color(58, 58, 58);
    public static final Color SELECTED = new Color(70, 70, 70);
    public static final Color ACCENT = new Color(50, 190, 193);
    public static final Color SUCCESS = new Color(94, 190, 126);
    public static final Color WARNING = new Color(235, 174, 76);
    public static final Color ERROR = new Color(226, 92, 92);
    public static final Color TEXT = new Color(232, 232, 232);
    public static final Color MUTED = new Color(171, 171, 171);
    public static final Color BORDER = new Color(17, 17, 17);

    private ClientTheme()
    {
    }

    public static void install()
    {
        // The game is an AWT heavyweight component. Swing's default lightweight
        // popups are otherwise painted underneath it and appear to flash closed.
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            | UnsupportedLookAndFeelException ignored)
        {
        }
        Font normal = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        Font bold = normal.deriveFont(Font.BOLD);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font", normal);
        UIManager.put("Button.background", CONTROL);
        UIManager.put("Button.foreground", TEXT);
        UIManager.put("Button.font", bold);
        UIManager.put("Button.focus", CONTROL);
        UIManager.put("ToggleButton.background", CONTROL);
        UIManager.put("ToggleButton.foreground", TEXT);
        UIManager.put("ToggleButton.select", SELECTED);
        UIManager.put("TextField.background", PANEL_DARK);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("PasswordField.background", PANEL_DARK);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("TextArea.background", PANEL_DARK);
        UIManager.put("TextArea.foreground", TEXT);
        UIManager.put("List.background", PANEL_DARK);
        UIManager.put("List.foreground", TEXT);
        UIManager.put("List.selectionBackground", SELECTED);
        UIManager.put("List.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.background", CONTROL);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("CheckBox.background", BACKGROUND);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("Spinner.background", CONTROL);
        UIManager.put("TabbedPane.background", BACKGROUND);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", PANEL);
        UIManager.put("MenuBar.background", PANEL_DARK);
        UIManager.put("MenuBar.foreground", TEXT);
        UIManager.put("MenuBar.border", BorderFactory.createEmptyBorder());
        UIManager.put("MenuBar.font", normal.deriveFont(12f));
        UIManager.put("Menu.background", PANEL_DARK);
        UIManager.put("Menu.foreground", TEXT);
        UIManager.put("Menu.font", normal.deriveFont(12f));
        UIManager.put("Menu.selectionBackground", SELECTED);
        UIManager.put("Menu.selectionForeground", Color.WHITE);
        UIManager.put("MenuItem.background", PANEL_DARK);
        UIManager.put("MenuItem.foreground", TEXT);
        UIManager.put("MenuItem.selectionBackground", SELECTED);
        UIManager.put("MenuItem.selectionForeground", Color.WHITE);
        UIManager.put("PopupMenu.background", PANEL_DARK);
        UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("CheckBoxMenuItem.background", PANEL_DARK);
        UIManager.put("CheckBoxMenuItem.foreground", TEXT);
        UIManager.put("CheckBoxMenuItem.selectionBackground", SELECTED);
        UIManager.put("Tree.background", PANEL_DARK);
        UIManager.put("Tree.foreground", TEXT);
        UIManager.put("Tree.selectionBackground", SELECTED);
        UIManager.put("Tree.selectionForeground", Color.WHITE);
        UIManager.put("Table.background", PANEL_DARK);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", SELECTED);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("TableHeader.background", CONTROL);
        UIManager.put("TableHeader.foreground", TEXT);
        UIManager.put("Separator.foreground", BORDER);
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("ToolTip.background", PANEL_DARK);
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER));
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT);
    }
}
