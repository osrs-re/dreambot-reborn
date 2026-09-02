package com.dreambotreborn.accounts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import com.dreambotreborn.ui.ClientTheme;
import com.dreambotreborn.ui.DreamWindow;

/** Separate window for managing plaintext local test accounts. */
public final class AccountManagerFrame
{
    private static final Color BACKGROUND = ClientTheme.BACKGROUND;
    private static final Color FOREGROUND = ClientTheme.TEXT;
    private static final Color MUTED = ClientTheme.MUTED;

    private final JFrame owner;
    private final JFrame frame = new JFrame("DreamBot Reborn — Accounts");
    private final AccountStore store;
    private final DefaultListModel<AccountSummary> accounts = new DefaultListModel<>();
    private final JList<AccountSummary> list = new JList<>(accounts);
    private final JLabel status = new JLabel("No accounts loaded");

    public AccountManagerFrame(JFrame owner, Path storagePath)
    {
        this.owner = owner;
        this.store = new AccountStore(storagePath);
        buildUi();
    }

    public void open()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(this::open);
            return;
        }
        if (!ensureLoaded())
        {
            return;
        }
        refreshAccounts();
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        frame.toFront();
    }

    /** Returns account choices for the Script Manager without an unlock prompt. */
    public List<AccountSummary> accountsForScripts()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            throw new IllegalStateException("Account selection must be opened on the Swing thread");
        }
        if (!ensureLoaded())
        {
            return Collections.emptyList();
        }
        return store.summaries();
    }

    /** Copies credentials briefly and prepares this exact account for script execution. */
    public CompletableFuture<AccountLoginResult> prepareAccountForScript(String accountId)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            throw new IllegalStateException("Account preparation must start on the Swing thread");
        }
        if (!ensureLoaded())
        {
            return CompletableFuture.completedFuture(
                AccountLoginResult.failure("Could not load accounts.json"));
        }
        try
        {
            return AccountLogin.ensureLoggedIn(store.credentials(accountId));
        }
        catch (AccountStore.StorageException | IllegalStateException ex)
        {
            return CompletableFuture.completedFuture(AccountLoginResult.failure(ex.getMessage()));
        }
    }

    private void buildUi()
    {
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(520, 350));
        frame.setSize(new Dimension(560, 390));

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setBackground(ClientTheme.PANEL_DARK);
        list.setForeground(FOREGROUND);
        list.setFixedCellHeight(42);
        list.setCellRenderer(new AccountRenderer());
        list.addListSelectionListener(event -> updateButtons());
        list.addMouseListener(new java.awt.event.MouseAdapter()
        {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event)
            {
                if (event.getClickCount() == 2)
                {
                    loginSelected();
                }
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        buttons.setBackground(BACKGROUND);
        JButton add = button("Add", this::addAccount);
        JButton edit = button("Edit", this::editSelected);
        JButton remove = button("Remove", this::removeSelected);
        JButton login = button("Login", this::loginSelected);
        edit.setName("requiresSelection");
        remove.setName("requiresSelection");
        login.setName("requiresSelection");
        buttons.add(add);
        buttons.add(edit);
        buttons.add(remove);
        buttons.add(login);

        status.setForeground(MUTED);
        status.setBorder(BorderFactory.createEmptyBorder(4, 10, 8, 10));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 4, 10));
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(ClientTheme.BORDER));
        root.add(scroll, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.NORTH);
        root.add(status, BorderLayout.SOUTH);
        DreamWindow.decorate(frame, root);
        updateButtons();
    }

    private boolean ensureLoaded()
    {
        try
        {
            store.load();
            return true;
        }
        catch (AccountStore.StorageException ex)
        {
            showError(ex.getMessage());
            return false;
        }
    }

    private void addAccount()
    {
        AccountForm values = showAccountForm(null);
        if (values == null)
        {
            return;
        }
        try
        {
            AccountSummary added = store.add(values.displayName, values.username, values.password);
            refreshAccounts();
            select(added.id);
            status.setText("Added " + added.displayName);
        }
        catch (AccountStore.StorageException ex)
        {
            showError(ex.getMessage());
        }
        finally
        {
            values.destroy();
        }
    }

    private void editSelected()
    {
        AccountSummary selected = list.getSelectedValue();
        if (selected == null)
        {
            return;
        }
        AccountForm values = showAccountForm(selected);
        if (values == null)
        {
            return;
        }
        try
        {
            store.update(selected.id, values.displayName, values.username, values.password);
            refreshAccounts();
            select(selected.id);
            status.setText("Updated " + values.displayName);
        }
        catch (AccountStore.StorageException ex)
        {
            showError(ex.getMessage());
        }
        finally
        {
            values.destroy();
        }
    }

    private void removeSelected()
    {
        AccountSummary selected = list.getSelectedValue();
        if (selected == null)
        {
            return;
        }
        int confirmed = JOptionPane.showConfirmDialog(
            frame,
            "Remove " + selected.displayName + " from saved accounts?\n"
                + "This saved entry cannot be recovered.",
            "Remove Account",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (confirmed != JOptionPane.OK_OPTION)
        {
            return;
        }
        try
        {
            store.remove(selected.id);
            refreshAccounts();
            status.setText("Removed " + selected.displayName);
        }
        catch (AccountStore.StorageException ex)
        {
            showError(ex.getMessage());
        }
    }

    private void loginSelected()
    {
        AccountSummary selected = list.getSelectedValue();
        if (selected == null)
        {
            return;
        }
        try
        {
            AccountCredentials credentials = store.credentials(selected.id);
            status.setText("Starting automatic login for " + selected.displayName + "…");
            AccountLogin.login(credentials).thenAccept(message -> SwingUtilities.invokeLater(() ->
                status.setText(message)));
        }
        catch (AccountStore.StorageException ex)
        {
            showError(ex.getMessage());
        }
    }

    private AccountForm showAccountForm(AccountSummary existing)
    {
        JTextField displayName = new JTextField(existing == null ? "" : existing.displayName, 24);
        JTextField username = new JTextField(existing == null ? "" : existing.username, 24);
        JPasswordField password = new JPasswordField(24);
        String passwordLabel = existing == null ? "Password" : "New password (optional)";
        JPanel panel = form(
            existing == null
                ? "Credentials are stored as plaintext local test data."
                : "Leave the password empty to keep the current password.",
            new String[] {"Display name", "Username / email", passwordLabel},
            new Component[] {displayName, username, password});
        int result = JOptionPane.showConfirmDialog(
            frame, panel, existing == null ? "Add Account" : "Edit Account",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION)
        {
            clear(password);
            return null;
        }
        char[] secret = password.getPassword();
        clear(password);
        return new AccountForm(displayName.getText(), username.getText(), secret);
    }

    private void refreshAccounts()
    {
        accounts.clear();
        for (AccountSummary summary : store.summaries())
        {
            accounts.addElement(summary);
        }
        if (!accounts.isEmpty() && list.getSelectedIndex() < 0)
        {
            list.setSelectedIndex(0);
        }
        status.setText(accounts.size() + " account" + (accounts.size() == 1 ? "" : "s")
            + " in accounts.json");
        updateButtons();
    }

    private void select(String id)
    {
        for (int index = 0; index < accounts.size(); index++)
        {
            if (accounts.get(index).id.equals(id))
            {
                list.setSelectedIndex(index);
                return;
            }
        }
    }

    private void updateButtons()
    {
        boolean selected = list.getSelectedValue() != null;
        if (frame.getContentPane() == null)
        {
            return;
        }
        setSelectionButtons(frame.getContentPane(), selected);
    }

    private static void setSelectionButtons(Component component, boolean enabled)
    {
        if (component instanceof JButton && "requiresSelection".equals(component.getName()))
        {
            component.setEnabled(enabled);
        }
        if (component instanceof java.awt.Container)
        {
            for (Component child : ((java.awt.Container) component).getComponents())
            {
                setSelectionButtons(child, enabled);
            }
        }
    }

    private JButton button(String text, Runnable action)
    {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void showError(String message)
    {
        JOptionPane.showMessageDialog(frame.isVisible() ? frame : owner,
            message, "Accounts", JOptionPane.ERROR_MESSAGE);
    }

    private static JPanel form(String description, String[] labels, Component[] fields)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 4, 12, 4);
        panel.add(new JLabel(description), constraints);
        constraints.gridwidth = 1;
        constraints.insets = new Insets(4, 4, 4, 8);
        for (int index = 0; index < labels.length; index++)
        {
            constraints.gridx = 0;
            constraints.gridy = index + 1;
            constraints.anchor = GridBagConstraints.EAST;
            panel.add(new JLabel(labels[index] + ':'), constraints);
            constraints.gridx = 1;
            constraints.anchor = GridBagConstraints.WEST;
            panel.add(fields[index], constraints);
        }
        return panel;
    }

    private static void clear(JPasswordField field)
    {
        field.setText("");
    }

    private static final class AccountRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean selected,
            boolean hasFocus)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, selected, hasFocus);
            if (value instanceof AccountSummary)
            {
                AccountSummary account = (AccountSummary) value;
                label.setText("<html><b>" + escape(account.displayName) + "</b><br>"
                    + escape(account.username) + "</html>");
                label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            }
            return label;
        }

        private static String escape(String value)
        {
            return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        }
    }

    private static final class AccountForm
    {
        private final String displayName;
        private final String username;
        private final char[] password;

        private AccountForm(String displayName, String username, char[] password)
        {
            this.displayName = displayName;
            this.username = username;
            this.password = password;
        }

        private void destroy()
        {
            Arrays.fill(password, '\0');
        }
    }
}
