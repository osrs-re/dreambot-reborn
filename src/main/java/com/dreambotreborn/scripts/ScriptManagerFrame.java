package com.dreambotreborn.scripts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Collections;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import com.dreambotreborn.api.script.AbstractScript;
import com.dreambotreborn.api.script.ScriptDescriptor;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.api.script.ScriptManifest;
import com.dreambotreborn.api.script.Category;
import com.dreambotreborn.api.input.VirtualMouse;
import com.dreambotreborn.accounts.AccountLoginResult;
import com.dreambotreborn.accounts.AccountManagerFrame;
import com.dreambotreborn.accounts.AccountSummary;
import com.dreambotreborn.ui.ClientTheme;
import com.dreambotreborn.ui.DreamWindow;

/** Separate window for discovering and controlling annotated scripts. */
public final class ScriptManagerFrame
{
    private static final Color BACKGROUND = ClientTheme.BACKGROUND;
    private static final Color PANEL = ClientTheme.PANEL_DARK;
    private static final Color FOREGROUND = ClientTheme.TEXT;
    private static final Color MUTED = ClientTheme.MUTED;
    private static final Color ACCENT = ClientTheme.SELECTED;

    private final JFrame owner;
    private final ScriptManager manager;
    private final AccountManagerFrame accountManager;
    private final JToggleButton physicalMouseInput;
    private final JFrame frame = new JFrame("DreamBot Reborn — Script Manager");
    private final DefaultListModel<ScriptDescriptor> scripts = new DefaultListModel<>();
    private final JList<ScriptDescriptor> list = new JList<>(scripts);
    private final DefaultComboBoxModel<AccountSummary> accounts = new DefaultComboBoxModel<>();
    private final JComboBox<AccountSummary> account = new JComboBox<>(accounts);
    private final JEditorPane details = new JEditorPane("text/html", "");
    private final JTextField search = new JTextField(18);
    private final JComboBox<String> category = new JComboBox<>();
    private final JLabel active = new JLabel("No script running");
    private final JLabel status = new JLabel(" ");
    private final JButton start = button("Start");
    private final JButton pause = button("Pause");
    private final JButton stop = button("Stop");
    private boolean preparingAccount;
    private List<ScriptDescriptor> discovered = Collections.emptyList();

    public ScriptManagerFrame(
        JFrame owner,
        ScriptManager manager,
        AccountManagerFrame accountManager,
        JToggleButton physicalMouseInput)
    {
        this.owner = owner;
        this.manager = manager;
        this.accountManager = accountManager;
        this.physicalMouseInput = physicalMouseInput;
        buildUi();
    }

    public void open()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(this::open);
            return;
        }
        refreshAll();
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        frame.toFront();
    }

    private void buildUi()
    {
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(700, 430));
        frame.setSize(new Dimension(760, 480));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(PANEL);
        list.setForeground(FOREGROUND);
        list.setSelectionBackground(ACCENT);
        list.setSelectionForeground(Color.WHITE);
        list.setFixedCellHeight(52);
        list.setCellRenderer(new ScriptRenderer());
        list.addListSelectionListener(event ->
        {
            if (!event.getValueIsAdjusting())
            {
                updateDetails();
            }
        });
        list.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event)
            {
                if (event.getClickCount() == 2)
                {
                    startSelected();
                }
            }
        });

        details.setEditable(false);
        details.setOpaque(true);
        details.setBackground(PANEL);
        details.setForeground(FOREGROUND);
        details.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        details.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        details.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane scriptScroll = new JScrollPane(list);
        JScrollPane detailsScroll = new JScrollPane(details);
        scriptScroll.setBorder(BorderFactory.createLineBorder(ClientTheme.BORDER));
        detailsScroll.setBorder(BorderFactory.createLineBorder(ClientTheme.BORDER));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scriptScroll, detailsScroll);
        split.setDividerLocation(285);
        split.setResizeWeight(0.4D);
        split.setBorder(null);

        account.setPreferredSize(new Dimension(310, 27));
        account.setMaximumRowCount(12);
        account.addActionListener(event -> updateRuntime());

        category.addItem("All categories");
        for (Category value : Category.values()) category.addItem(value.name());
        category.addActionListener(event -> applyFilter(null));
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
        {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { applyFilter(null); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { applyFilter(null); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { applyFilter(null); }
        });

        JButton refresh = button("Refresh");
        JButton directory = button("Scripts folder");
        refresh.addActionListener(event -> refreshAll());
        directory.addActionListener(event -> openScriptsDirectory());
        start.addActionListener(event -> startSelected());
        pause.addActionListener(event -> togglePause());
        stop.addActionListener(event ->
        {
            if (manager.stop())
            {
                status.setText("Stopping script…");
            }
            updateRuntime();
        });

        JPanel discoveryActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        discoveryActions.setBackground(BACKGROUND);
        discoveryActions.add(refresh);
        discoveryActions.add(directory);

        JPanel lifecycleActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        lifecycleActions.setBackground(BACKGROUND);
        lifecycleActions.add(start);
        lifecycleActions.add(pause);
        lifecycleActions.add(stop);

        JPanel toolRow = new JPanel(new BorderLayout());
        toolRow.setBackground(BACKGROUND);
        toolRow.add(discoveryActions, BorderLayout.WEST);
        toolRow.add(lifecycleActions, BorderLayout.EAST);

        JLabel accountLabel = new JLabel("Account:");
        accountLabel.setForeground(FOREGROUND);
        JPanel accountRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        accountRow.setBackground(BACKGROUND);
        accountRow.add(accountLabel);
        accountRow.add(account);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filterRow.setBackground(BACKGROUND);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(FOREGROUND);
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setForeground(FOREGROUND);
        filterRow.add(searchLabel);
        filterRow.add(search);
        filterRow.add(categoryLabel);
        filterRow.add(category);

        JPanel actions = new JPanel(new BorderLayout());
        actions.setBackground(BACKGROUND);
        actions.add(accountRow, BorderLayout.NORTH);
        actions.add(filterRow, BorderLayout.CENTER);
        actions.add(toolRow, BorderLayout.SOUTH);

        active.setForeground(FOREGROUND);
        active.setBorder(BorderFactory.createEmptyBorder(6, 10, 2, 10));
        status.setForeground(MUTED);
        status.setBorder(BorderFactory.createEmptyBorder(2, 10, 8, 10));
        JPanel footer = new JPanel(new GridLayout(2, 1));
        footer.setBackground(BACKGROUND);
        footer.add(active);
        footer.add(status);

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        root.add(actions, BorderLayout.NORTH);
        root.add(split, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        DreamWindow.decorate(frame, root);

        Timer runtimeUpdates = new Timer(250, event -> updateRuntime());
        runtimeUpdates.start();
        updateRuntime();
    }

    private void refreshScripts()
    {
        String selectedClass = list.getSelectedValue() == null
            ? null : list.getSelectedValue().getClassName();
        List<ScriptDescriptor> found = manager.reloadScripts();
        discovered = found;
        applyFilter(selectedClass);
        updateRuntime();
    }

    private void applyFilter(String selectedClass)
    {
        if (selectedClass == null && list.getSelectedValue() != null)
            selectedClass = list.getSelectedValue().getClassName();
        String query = search.getText().trim().toLowerCase(Locale.ROOT);
        String selectedCategory = (String) category.getSelectedItem();
        scripts.clear();
        for (ScriptDescriptor descriptor : discovered)
        {
            ScriptManifest manifest = descriptor.getManifest();
            String haystack = (manifest.name() + ' ' + manifest.author() + ' '
                + manifest.description()).toLowerCase(Locale.ROOT);
            boolean categoryMatches = selectedCategory == null
                || "All categories".equals(selectedCategory)
                || manifest.category().name().equals(selectedCategory);
            if (categoryMatches && (query.isEmpty() || haystack.contains(query)))
                scripts.addElement(descriptor);
        }
        if (selectedClass != null)
        {
            for (int index = 0; index < scripts.size(); index++)
            {
                if (scripts.get(index).getClassName().equals(selectedClass))
                {
                    list.setSelectedIndex(index);
                    break;
                }
            }
        }
        if (!scripts.isEmpty() && list.getSelectedIndex() < 0) list.setSelectedIndex(0);
        updateDetails();
    }

    private void refreshAll()
    {
        refreshScripts();
        refreshAccounts();
    }

    private void refreshAccounts()
    {
        AccountSummary selected = (AccountSummary) account.getSelectedItem();
        String selectedId = selected == null ? null : selected.id;
        accounts.removeAllElements();
        for (AccountSummary summary : accountManager.accountsForScripts())
        {
            accounts.addElement(summary);
        }
        if (selectedId != null)
        {
            for (int index = 0; index < accounts.getSize(); index++)
            {
                if (accounts.getElementAt(index).id.equals(selectedId))
                {
                    account.setSelectedIndex(index);
                    break;
                }
            }
        }
        if (accounts.getSize() > 0 && account.getSelectedIndex() < 0)
        {
            account.setSelectedIndex(0);
        }

        int errors = manager.getDiscoveryErrors().size();
        if (accounts.getSize() == 0)
        {
            status.setText("No account selected — add one in Accounts");
        }
        else
        {
            status.setText(scripts.size() + " of " + discovered.size() + " script(s) • "
                + accounts.getSize() + " account(s)"
                + (errors == 0 ? "" : " • " + errors + " script load error(s)"));
        }
        updateRuntime();
    }

    private void startSelected()
    {
        ScriptDescriptor selected = list.getSelectedValue();
        if (selected == null)
        {
            status.setText("Select a script first");
            return;
        }
        AccountSummary selectedAccount = (AccountSummary) account.getSelectedItem();
        if (selectedAccount == null)
        {
            status.setText("Select an account before starting a script");
            return;
        }
        if (preparingAccount)
        {
            return;
        }

        manager.stop();
        disablePhysicalMouseInput();
        preparingAccount = true;
        status.setText("Preparing account " + selectedAccount.displayName + "…");
        updateRuntime();
        accountManager.prepareAccountForScript(selectedAccount.id)
            .whenComplete((result, error) -> SwingUtilities.invokeLater(() ->
            {
                preparingAccount = false;
                if (error != null)
                {
                    showError("Could not prepare account", error);
                }
                else if (result == null || !result.isSuccessful())
                {
                    AccountLoginResult failure = result == null
                        ? AccountLoginResult.failure("Account preparation returned no result") : result;
                    status.setText(failure.getMessage());
                }
                else
                {
                    try
                    {
                        manager.start(selected.getScriptClass());
                        manager.configureLoginRecovery(() ->
                            accountManager.prepareAccountForScript(selectedAccount.id)
                                .thenApply(AccountLoginResult::isSuccessful));
                        status.setText("Started " + selected.getManifest().name()
                            + " with " + selectedAccount.displayName);
                    }
                    catch (RuntimeException ex)
                    {
                        showError("Could not start script", ex);
                    }
                }
                updateRuntime();
            }));
    }

    /** Main-toolbar action: opens selection when needed, otherwise runs it. */
    public void startSelectedScript()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(this::startSelectedScript);
            return;
        }
        if (list.getSelectedValue() == null || account.getSelectedItem() == null)
        {
            open();
            return;
        }
        startSelected();
    }

    public void togglePaused()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(this::togglePaused);
            return;
        }
        togglePause();
    }

    public void reload()
    {
        if (!SwingUtilities.isEventDispatchThread()) SwingUtilities.invokeLater(this::reload);
        else refreshAll();
    }

    private void disablePhysicalMouseInput()
    {
        if (physicalMouseInput.isSelected())
        {
            physicalMouseInput.doClick();
        }
        else
        {
            VirtualMouse.setPhysicalInputEnabled(false);
        }
    }

    private void togglePause()
    {
        if (manager.isPaused())
        {
            if (manager.resume())
            {
                status.setText("Script resumed");
            }
        }
        else if (manager.pause())
        {
            status.setText("Script paused");
        }
        updateRuntime();
    }

    private void updateRuntime()
    {
        AbstractScript script = manager.getCurrentScript();
        boolean running = script != null && manager.isRunning();
        if (script == null)
        {
            active.setText("No script running");
        }
        else
        {
            ScriptManifest manifest = script.getManifest();
            active.setText(manifest.name() + "  •  " + manager.getState()
                + "  •  " + script.getScriptTimer().formatTime());
        }
        start.setEnabled(!preparingAccount && list.getSelectedValue() != null
            && account.getSelectedItem() != null);
        account.setEnabled(!preparingAccount);
        pause.setEnabled(running);
        pause.setText(manager.isPaused() ? "Resume" : "Pause");
        stop.setEnabled(running);

        Throwable error = manager.getLastError();
        if (error != null && (status.getText().trim().isEmpty()
            || !status.getText().startsWith("Error:")))
        {
            status.setText("Error: " + error.getClass().getSimpleName() + " — " + error.getMessage());
        }
    }

    private void updateDetails()
    {
        ScriptDescriptor selected = list.getSelectedValue();
        if (selected == null)
        {
            details.setText("<html><body style='color:#dcdfe4'>No scripts found.</body></html>");
            details.setCaretPosition(0);
            return;
        }
        ScriptManifest manifest = selected.getManifest();
        Path source = selected.getSource();
        details.setText("<html><body style='font-family:sans-serif;color:#e8e8e8;background:#1f1f1f'>"
            + "<h2 style='margin-bottom:3px'>" + html(manifest.name()) + "</h2>"
            + "<div style='color:#9ba0a9'>Version " + manifest.version() + " &nbsp;•&nbsp; "
            + html(manifest.category().name()) + "</div>"
            + "<p>" + html(manifest.description()).replace("\n", "<br>") + "</p>"
            + "<p><b>Author</b><br>" + html(manifest.author()) + "</p>"
            + "<p><b>Class</b><br><code>" + html(selected.getClassName()) + "</code></p>"
            + "<p><b>Source</b><br><code>" + html(source == null ? "application" : source.toString())
            + "</code></p></body></html>");
        details.setCaretPosition(0);
    }

    private void openScriptsDirectory()
    {
        try
        {
            java.nio.file.Files.createDirectories(manager.getScriptsDirectory());
            if (!Desktop.isDesktopSupported())
            {
                throw new IOException("Desktop integration is unavailable");
            }
            Desktop.getDesktop().open(manager.getScriptsDirectory().toFile());
        }
        catch (IOException | UnsupportedOperationException ex)
        {
            showError("Could not open scripts folder", ex);
        }
    }

    private void showError(String title, Throwable error)
    {
        status.setText("Error: " + error.getMessage());
        JOptionPane.showMessageDialog(frame, error.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    private static JButton button(String text)
    {
        JButton button = new JButton(text);
        button.setFocusable(false);
        return button;
    }

    private static String html(String value)
    {
        return value == null ? "" : value.replace("&", "&amp;")
            .replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static final class ScriptRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean focused)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, selected, focused);
            ScriptDescriptor descriptor = (ScriptDescriptor) value;
            ScriptManifest manifest = descriptor.getManifest();
            label.setText("<html><b>" + html(manifest.name()) + "</b><br>"
                + "<span style='color:" + (selected ? "#ffffff" : "#9ba0a9") + "'>"
                + html(manifest.author()) + " • v" + manifest.version() + "</span></html>");
            label.setBorder(BorderFactory.createEmptyBorder(5, 9, 5, 9));
            return label;
        }
    }
}
