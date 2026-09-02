package com.dreambotreborn.scripts;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import com.dreambotreborn.accounts.AccountLoginResult;
import com.dreambotreborn.accounts.AccountManagerFrame;
import com.dreambotreborn.accounts.AccountSummary;
import com.dreambotreborn.api.script.ScriptDescriptor;
import com.dreambotreborn.api.script.ScriptManager;
import com.dreambotreborn.api.script.schedule.ScheduledScript;
import com.dreambotreborn.api.script.schedule.ScriptSchedule;
import com.dreambotreborn.api.script.schedule.ScriptScheduler;
import com.dreambotreborn.api.script.schedule.conditions.RunningTime;
import com.dreambotreborn.ui.ClientTheme;
import com.dreambotreborn.ui.DreamWindow;

/** DreamBot-style visual schedule builder for sequential account/script sessions. */
public final class ScheduleManagerFrame
{
    private final JFrame owner;
    private final ScriptManager scripts;
    private final AccountManagerFrame accounts;
    private final JFrame frame = new JFrame("DreamBot Reborn — Script Schedule");
    private final DefaultListModel<ScheduledScript> entries = new DefaultListModel<>();
    private final JList<ScheduledScript> list = new JList<>(entries);
    private final DefaultComboBoxModel<String> scriptModel = new DefaultComboBoxModel<>();
    private final JComboBox<String> script = new JComboBox<>(scriptModel);
    private final DefaultComboBoxModel<AccountSummary> accountModel = new DefaultComboBoxModel<>();
    private final JComboBox<AccountSummary> account = new JComboBox<>(accountModel);
    private final JTextField parameters = new JTextField(18);
    private final JSpinner minutes = new JSpinner(new SpinnerNumberModel(30, 1, 1440, 5));
    private final JCheckBox repeat = new JCheckBox("Repeat schedule");
    private final JLabel status = new JLabel("Idle");

    public ScheduleManagerFrame(
        JFrame owner, ScriptManager scripts, AccountManagerFrame accounts)
    {
        this.owner = owner;
        this.scripts = scripts;
        this.accounts = accounts;
        ScriptScheduler.getScriptScheduler().setAccountPreparer(this::prepareAccount);
        buildUi();
    }

    public void open()
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(this::open);
            return;
        }
        refreshChoices();
        frame.setLocationRelativeTo(owner);
        frame.setVisible(true);
        frame.toFront();
    }

    private void buildUi()
    {
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(680, 420));
        frame.setSize(720, 460);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ClientTheme.BORDER), "Add session"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        addField(form, c, "Script", script);
        addField(form, c, "Account", account);
        addField(form, c, "Minutes", minutes);
        addField(form, c, "Arguments", parameters);
        JButton add = new JButton("Add to queue");
        add.setFocusable(false);
        add.addActionListener(event -> addEntry());
        c.gridx = 0;
        c.gridwidth = 2;
        form.add(add, c);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(44);
        list.setCellRenderer(new EntryRenderer());
        JScrollPane scroll = new JScrollPane(list);

        JButton remove = new JButton("Remove");
        JButton clear = new JButton("Clear");
        JButton start = new JButton("Start schedule");
        JButton stop = new JButton("Stop");
        remove.addActionListener(event ->
        {
            int index = list.getSelectedIndex();
            if (index >= 0) entries.remove(index);
        });
        clear.addActionListener(event -> entries.clear());
        start.addActionListener(event -> startSchedule());
        stop.addActionListener(event -> ScriptScheduler.getScriptScheduler().reset());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        controls.add(remove);
        controls.add(clear);
        controls.add(repeat);
        controls.add(start);
        controls.add(stop);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 8, 10));
        root.add(form, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(controls, BorderLayout.CENTER);
        status.setForeground(ClientTheme.MUTED);
        footer.add(status, BorderLayout.SOUTH);
        root.add(footer, BorderLayout.SOUTH);
        DreamWindow.decorate(frame, root);
        new Timer(250, event -> status.setText(
            ScriptScheduler.getScriptScheduler().getStatus())).start();
    }

    private void refreshChoices()
    {
        scripts.reloadScripts();
        scriptModel.removeAllElements();
        for (ScriptDescriptor descriptor : scripts.getDiscoveredScripts())
            scriptModel.addElement(descriptor.getManifest().name());
        accountModel.removeAllElements();
        for (AccountSummary summary : accounts.accountsForScripts()) accountModel.addElement(summary);
    }

    private void addEntry()
    {
        String scriptName = (String) script.getSelectedItem();
        AccountSummary selectedAccount = (AccountSummary) account.getSelectedItem();
        if (scriptName == null || selectedAccount == null)
        {
            status.setText("Select both a script and account");
            return;
        }
        int duration = ((Number) minutes.getValue()).intValue();
        ScheduledScript entry = new ScheduledScript();
        entry.setScript(scriptName);
        entry.setAccountId(selectedAccount.id);
        entry.setAccountName(selectedAccount.displayName);
        entry.setParametersString(parameters.getText());
        entry.setStopCondition(new RunningTime(Duration.ofMinutes(duration), Duration.ofMinutes(duration)));
        entries.addElement(entry);
        list.setSelectedIndex(entries.size() - 1);
        status.setText("Added " + scriptName);
    }

    private void startSchedule()
    {
        if (entries.isEmpty())
        {
            status.setText("Add at least one session first");
            return;
        }
        List<ScheduledScript> copy = new ArrayList<>();
        for (int index = 0; index < entries.size(); index++) copy.add(entries.get(index));
        ScriptSchedule schedule = new ScriptSchedule();
        schedule.setName("DreamBot Reborn schedule");
        schedule.setRepeating(repeat.isSelected());
        schedule.setScheduledScripts(copy);
        if (!ScriptScheduler.getScriptScheduler().start(schedule))
            status.setText("A schedule is already running");
    }

    private CompletableFuture<Boolean> prepareAccount(String id)
    {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        SwingUtilities.invokeLater(() ->
        {
            if (id == null || id.isEmpty())
            {
                result.complete(false);
                return;
            }
            try
            {
                accounts.prepareAccountForScript(id).whenComplete((login, error) ->
                {
                    if (error != null) result.completeExceptionally(error);
                    else result.complete(login != null && login.isSuccessful());
                });
            }
            catch (RuntimeException error)
            {
                result.completeExceptionally(error);
            }
        });
        return result;
    }

    private static void addField(
        JPanel form, GridBagConstraints c, String label, Component field)
    {
        c.gridx = 0;
        c.gridwidth = 1;
        c.weightx = 0;
        form.add(new JLabel(label + ':'), c);
        c.gridx = 1;
        c.weightx = 1;
        form.add(field, c);
        c.gridy++;
    }

    private static final class EntryRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list, Object value, int index, boolean selected, boolean focused)
        {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, selected, focused);
            ScheduledScript entry = (ScheduledScript) value;
            label.setText("<html><b>" + escape(entry.getScript()) + "</b><br>Account: "
                + escape(entry.getAccountName().isEmpty()
                    ? entry.getAccountId() : entry.getAccountName()) + " • "
                + escape(entry.getParametersString()) + "</html>");
            label.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            return label;
        }

        private static String escape(String value)
        {
            return value == null ? "" : value.replace("&", "&amp;")
                .replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
