package com.dreambotreborn.devtools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import com.dreambotreborn.ui.ClientTheme;

/** Swing panel for browsing and selecting the live widget hierarchy. */
public final class WidgetInspectorPanel extends JPanel
{
    private static final Color BACKGROUND = ClientTheme.BACKGROUND;
    private static final Color FIELD_BACKGROUND = ClientTheme.PANEL_DARK;
    private static final Color FOREGROUND = ClientTheme.TEXT;
    private static final Color MUTED = ClientTheme.MUTED;
    private static final Color SELECTION = ClientTheme.SELECTED;

    private final JTextField filter = new JTextField();
    private final JTree tree = new JTree();
    private final JTextArea details = new JTextArea();
    private final JLabel status = new JLabel("Waiting for widgets…");
    private final JToggleButton picker = new JToggleButton("Pick");
    private long renderedVersion = -1L;
    private boolean adjustingSelection;

    public WidgetInspectorPanel()
    {
        super(new BorderLayout(0, 8));
        setOpaque(true);
        setBackground(BACKGROUND);

        JPanel controls = new JPanel(new BorderLayout(0, 6));
        controls.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        buttons.setOpaque(false);
        styleButton(picker);
        picker.setToolTipText("Select a widget directly on the game canvas");
        picker.addActionListener(event -> WidgetInspector.setPickerEnabled(picker.isSelected()));
        buttons.add(picker);

        JCheckBox highlight = new JCheckBox("Highlight", true);
        highlight.setOpaque(false);
        highlight.setForeground(FOREGROUND);
        highlight.setFocusable(false);
        highlight.addActionListener(event -> WidgetInspector.setHighlightEnabled(highlight.isSelected()));
        buttons.add(highlight);

        JButton refresh = new JButton("Refresh");
        styleButton(refresh);
        refresh.addActionListener(event -> WidgetInspector.requestRefresh());
        buttons.add(refresh);
        controls.add(buttons, BorderLayout.NORTH);

        filter.setToolTipText("Filter by id, type, name, text, action, or child kind");
        filter.setBackground(FIELD_BACKGROUND);
        filter.setForeground(FOREGROUND);
        filter.setCaretColor(FOREGROUND);
        filter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ClientTheme.BORDER),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)));
        filter.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent event)
            {
                rebuildTree();
            }

            @Override
            public void removeUpdate(DocumentEvent event)
            {
                rebuildTree();
            }

            @Override
            public void changedUpdate(DocumentEvent event)
            {
                rebuildTree();
            }
        });
        controls.add(filter, BorderLayout.SOUTH);
        add(controls, BorderLayout.NORTH);

        configureTree();
        JScrollPane treeScroll = scrollPane(tree);

        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 11));
        details.setBackground(FIELD_BACKGROUND);
        details.setForeground(FOREGROUND);
        details.setCaretColor(FOREGROUND);
        details.setText("Select a widget to inspect its properties.");
        JScrollPane detailScroll = scrollPane(details);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScroll, detailScroll);
        split.setBorder(null);
        split.setDividerSize(5);
        split.setResizeWeight(0.68);
        split.setOpaque(false);
        split.setPreferredSize(new Dimension(255, 410));
        add(split, BorderLayout.CENTER);

        status.setForeground(MUTED);
        add(status, BorderLayout.SOUTH);

        Timer timer = new Timer(250, event -> update());
        timer.setCoalesce(true);
        timer.start();
    }

    private void configureTree()
    {
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("Widgets")));
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(20);
        tree.setBackground(FIELD_BACKGROUND);
        tree.setForeground(FOREGROUND);
        tree.setCellRenderer(new WidgetTreeRenderer());
        tree.getSelectionModel().setSelectionMode(
            javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(event ->
        {
            if (adjustingSelection)
            {
                return;
            }
            Object value = tree.getLastSelectedPathComponent();
            WidgetSnapshot selected = value instanceof WidgetNode
                ? ((WidgetNode) value).widget
                : null;
            WidgetInspector.select(selected == null ? null : selected.key);
            showDetails(selected);
        });
    }

    private void update()
    {
        WidgetTreeSnapshot current = WidgetInspector.getSnapshot();
        if (current.version != renderedVersion)
        {
            rebuildTree();
        }
        synchronizeSelection();
        picker.setSelected(WidgetInspector.isPickerEnabled());
    }

    private void rebuildTree()
    {
        WidgetTreeSnapshot current = WidgetInspector.getSnapshot();
        Set<String> expanded = expandedKeys();
        String query = filter.getText().trim().toLowerCase(Locale.ROOT);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(
            query.isEmpty() ? "Widgets (" + current.widgetCount + ')' : "Filtered widgets");

        int visibleRoots = 0;
        for (WidgetSnapshot widget : current.roots)
        {
            WidgetNode node = filteredNode(widget, query);
            if (node != null)
            {
                root.add(node);
                visibleRoots++;
            }
        }
        if (current.widgetCount == 0 && query.isEmpty())
        {
            DefaultMutableTreeNode notice = new DefaultMutableTreeNode(
                "No widget interface loaded");
            notice.add(new DefaultMutableTreeNode("Log in to load game widgets"));
            notice.add(new DefaultMutableTreeNode("The login screen is painted directly"));
            root.add(notice);
        }

        adjustingSelection = true;
        try
        {
            tree.setModel(new DefaultTreeModel(root));
            tree.expandRow(0);
            if (current.roots.size() == 1)
            {
                tree.expandRow(1);
            }
            restoreExpanded(new TreePath(root), expanded);
        }
        finally
        {
            adjustingSelection = false;
        }
        renderedVersion = current.version;
        status.setText(current.widgetCount + " widgets • " + visibleRoots + " roots • "
            + current.source + (query.isEmpty() ? "" : " • filtered"));
        synchronizeSelection();
    }

    private WidgetNode filteredNode(WidgetSnapshot widget, String query)
    {
        WidgetNode node = new WidgetNode(widget);
        for (WidgetSnapshot child : widget.children)
        {
            WidgetNode childNode = filteredNode(child, query);
            if (childNode != null)
            {
                node.add(childNode);
            }
        }
        boolean matches = query.isEmpty() || widget.searchText().contains(query);
        return matches || node.getChildCount() > 0 ? node : null;
    }

    private void synchronizeSelection()
    {
        String selectedKey = WidgetInspector.getSelectedKey();
        Object current = tree.getLastSelectedPathComponent();
        String treeKey = current instanceof WidgetNode ? ((WidgetNode) current).widget.key : null;
        if (selectedKey == null ? treeKey == null : selectedKey.equals(treeKey))
        {
            showDetails(WidgetInspector.getSelectedWidget());
            return;
        }

        TreePath path = findPath((DefaultMutableTreeNode) tree.getModel().getRoot(), selectedKey);
        adjustingSelection = true;
        try
        {
            if (path == null)
            {
                tree.clearSelection();
            }
            else
            {
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
            }
        }
        finally
        {
            adjustingSelection = false;
        }
        showDetails(WidgetInspector.getSelectedWidget());
    }

    private static TreePath findPath(DefaultMutableTreeNode root, String key)
    {
        if (key == null)
        {
            return null;
        }
        Enumeration<?> nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements())
        {
            Object value = nodes.nextElement();
            if (value instanceof WidgetNode && key.equals(((WidgetNode) value).widget.key))
            {
                return new TreePath(((WidgetNode) value).getPath());
            }
        }
        return null;
    }

    private Set<String> expandedKeys()
    {
        Object root = tree.getModel().getRoot();
        if (!(root instanceof DefaultMutableTreeNode))
        {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        Enumeration<?> nodes = ((DefaultMutableTreeNode) root).breadthFirstEnumeration();
        while (nodes.hasMoreElements())
        {
            Object value = nodes.nextElement();
            if (value instanceof WidgetNode)
            {
                WidgetNode node = (WidgetNode) value;
                if (tree.isExpanded(new TreePath(node.getPath())))
                {
                    result.add(node.widget.key);
                }
            }
        }
        return result;
    }

    private void restoreExpanded(TreePath path, Set<String> expanded)
    {
        Object value = path.getLastPathComponent();
        if (value instanceof WidgetNode && expanded.contains(((WidgetNode) value).widget.key))
        {
            tree.expandPath(path);
        }
        if (value instanceof DefaultMutableTreeNode)
        {
            Enumeration<?> children = ((DefaultMutableTreeNode) value).children();
            while (children.hasMoreElements())
            {
                restoreExpanded(path.pathByAddingChild(children.nextElement()), expanded);
            }
        }
    }

    private void showDetails(WidgetSnapshot widget)
    {
        if (widget == null)
        {
            details.setText("Select a widget to inspect its properties.");
            return;
        }

        StringBuilder value = new StringBuilder();
        if (widget.isLoginElement())
        {
            line(value, "Element", widget.name);
            line(value, "Relation", widget.relation);
            line(value, "Type", widget.typeName);
            line(value, "Bounds", widget.bounds == null ? "none" :
                widget.bounds.x + ", " + widget.bounds.y + "  "
                    + widget.bounds.width + "×" + widget.bounds.height);
            if (!widget.text.isEmpty())
            {
                line(value, "State", widget.text);
            }
            if (!widget.actions.isEmpty())
            {
                line(value, "Actions", String.join(", ", widget.actions));
            }
            line(value, "Children", widget.children.size());
            details.setText(value.toString());
            details.setCaretPosition(0);
            return;
        }

        line(value, "ID", widget.groupId + ":" + widget.childId + " (" + widget.id + ")");
        line(value, "Index", widget.index);
        line(value, "Relation", widget.relation);
        line(value, "Type", widget.typeName + " (" + widget.type + ")");
        line(value, "Content", widget.contentType);
        line(value, "Bounds", widget.bounds == null ? "none" :
            widget.bounds.x + ", " + widget.bounds.y + "  "
                + widget.bounds.width + "×" + widget.bounds.height);
        line(value, "Hidden", widget.hidden + " (self: " + widget.selfHidden + ")");
        if (!widget.name.isEmpty())
        {
            line(value, "Name", widget.name);
        }
        if (!widget.text.isEmpty())
        {
            line(value, "Text", widget.text);
        }
        if (widget.itemId >= 0)
        {
            line(value, "Item", widget.itemId + " × " + widget.itemQuantity);
        }
        if (!widget.actions.isEmpty())
        {
            line(value, "Actions", String.join(", ", widget.actions));
        }
        if (widget.scrollWidth > 0 || widget.scrollHeight > 0)
        {
            line(value, "Scroll", widget.scrollX + ", " + widget.scrollY + " / "
                + widget.scrollWidth + "×" + widget.scrollHeight);
        }
        line(value, "Children", widget.children.size());
        details.setText(value.toString());
        details.setCaretPosition(0);
    }

    private static void line(StringBuilder destination, String name, Object value)
    {
        destination.append(name).append(": ").append(value).append('\n');
    }

    private static JScrollPane scrollPane(Component component)
    {
        JScrollPane scroll = new JScrollPane(component);
        scroll.setBorder(BorderFactory.createLineBorder(ClientTheme.BORDER));
        scroll.getViewport().setBackground(FIELD_BACKGROUND);
        return scroll;
    }

    private static void styleButton(javax.swing.AbstractButton button)
    {
        button.setFocusPainted(false);
        button.setFocusable(false);
        button.setForeground(FOREGROUND);
        button.setBackground(ClientTheme.CONTROL);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ClientTheme.BORDER),
            BorderFactory.createEmptyBorder(4, 7, 4, 7)));
    }

    private static final class WidgetNode extends DefaultMutableTreeNode
    {
        private final WidgetSnapshot widget;

        private WidgetNode(WidgetSnapshot widget)
        {
            super(widget);
            this.widget = widget;
        }

        @Override
        public String toString()
        {
            return widget.displayName();
        }
    }

    private static final class WidgetTreeRenderer extends DefaultTreeCellRenderer
    {
        @Override
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
            setBackgroundNonSelectionColor(FIELD_BACKGROUND);
            setBackgroundSelectionColor(SELECTION);
            setTextNonSelectionColor(value instanceof WidgetNode && ((WidgetNode) value).widget.hidden
                ? MUTED : FOREGROUND);
            setTextSelectionColor(Color.WHITE);
            setLeafIcon(null);
            setClosedIcon(null);
            setOpenIcon(null);
            return this;
        }
    }
}
