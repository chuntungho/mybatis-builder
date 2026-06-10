/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.MybatisIcons;
import com.chuntung.plugin.mybatis.builder.action.SettingsPresenter;
import com.chuntung.plugin.mybatis.builder.database.DriverDownloader;
import com.chuntung.plugin.mybatis.builder.generator.plugins.RenamePlugin;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.CustomDriverInfo;
import com.chuntung.plugin.mybatis.builder.model.DriverTypeEnum;
import com.chuntung.plugin.mybatis.builder.model.ObjectTableModel;
import com.chuntung.plugin.mybatis.builder.model.PropertyEntry;
import com.chuntung.plugin.mybatis.builder.util.JsonUtil;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.chuntung.plugin.mybatis.builder.util.ViewUtil;
import com.chuntung.plugin.mybatis.builder.generator.DefaultParameters;
import com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock.SelectWithLockConfig;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.config.ModelType;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MybatisBuilderSettingsDialog extends DialogWrapper {
    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, false, false, false);
    private JPanel contentPanel;
    private JList connectionList;
    private JTextField connectionNameText;
    private JComboBox driverTypeComboBox;
    private TextFieldWithBrowseButton driverLibraryText;
    private JTextField driverClassText;
    private JTextField urlText;
    private JTextField descriptionText;
    private JPanel driverPanel;
    private JTextField hostText;
    private JSpinner portSpinner;
    private JTextField userText;
    private JPasswordField passwordText;
    private JTextField databaseText;
    private JCheckBox activeCheckBox;
    private JButton testConnectionButton;
    private JTextField javaFileEncodingText;
    private JComboBox defaultModelTypeComboBox;
    private JPanel hostPanel;
    private JPanel connectionPanel;
    private JTextField customAnnotationTypeText;
    private JTextField byPrimaryKeyOverrideText;
    private JTextField byExampleOverrideText;
    private JTextField mapperTypePatternText;
    private JTextField exampleTypePatternText;
    private JTextField sqlFileNamePatternText;
    private JTextField generatedCommentText;
    private JCheckBox forceBigDecimalsCheckbox;
    private JSpinner historySizeSpinner;
    private JButton clearAllButton;
    private JCheckBox useJSR310TypesCheckBox;
    private JLabel urlLabel;
    private JPanel listPanel;
    private JTabbedPane connectionTabs;
    private JPanel generalTab;
    private JPanel propertiesTab;
    private JPanel connectionContainer;
    private JLabel connectionEmptyLabel;
    private JPanel propertiesPanel;
    private JScrollPane propertiesScrollPane;
    private JTable propertiesTable;
    private JLabel hostLabel;
    private JLabel portLabel;
    private JLabel databaseLabel;
    private JPanel driverStatusPanel;
    private JLabel driverStatusLabel;
    private LinkLabel downloadDriverLink;

    private final SettingsPresenter settingsHandler;
    private Project project;
    private ConnectionInfo current;
    private Action applyAction;
    // user-registered drivers, loaded once and refreshed after the Register dialog
    private List<CustomDriverInfo> customDrivers = new ArrayList<>();

    private SettingsSnapshot baseline;
    private boolean loading;
    private final DocumentListener textDirtyListener = new DocumentAdapter() {
        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            refreshDirty();
        }
    };
    private final ItemListener itemDirtyListener = e -> refreshDirty();
    private final ChangeListener changeDirtyListener = e -> refreshDirty();

    private record SettingsSnapshot(String json) {
        boolean differsFrom(SettingsSnapshot other) {
            return other == null || !json.equals(other.json);
        }
    }

    public MybatisBuilderSettingsDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        settingsHandler = SettingsPresenter.getInstance(project);

        initGUI();

        init();
    }

    private void initGUI() {
        setTitle("MyBatis Builder - Settings");

        // default parameters
        initDefaultParameterPane();

        // connection info list
        List<ConnectionInfo> connectionInfoList = settingsHandler.loadConnectionInfoList();
        DefaultListModel<ConnectionInfo> listModel = new DefaultListModel();
        for (ConnectionInfo connectionInfo : connectionInfoList) {
            listModel.addElement(connectionInfo);
        }
        connectionList.setModel(listModel);
        listModel.addListDataListener(new ListDataListener() {
            @Override public void intervalAdded(ListDataEvent e) { refreshDirty(); }
            @Override public void intervalRemoved(ListDataEvent e) { refreshDirty(); }
            @Override public void contentsChanged(ListDataEvent e) { refreshDirty(); }
        });
        connectionList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                ConnectionInfo item = (ConnectionInfo) value;
                super.getListCellRendererComponent(list, item.getName(), index, isSelected, cellHasFocus);
                if (item.getDriverType() != null) {
                    setIcon(MybatisIcons.load(item.getDriverType().getIcon()));
                }
                return this;
            }
        });
        connectionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                doSelect((JList) e.getSource());
            }
        });

        showEmptyState(true);

        driverLibraryText.addBrowseFolderListener("Choose Library", "Library should contain java.sql.Driver implement ", project, LIBRARY_FILE_DESCRIPTOR);
        driverPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Override (optional)"));

        portSpinner.setModel(new SpinnerNumberModel(3306, 80, 65536, 1));
        portSpinner.setEditor(new JSpinner.NumberEditor(portSpinner, "#"));

        // registered drivers, used by the Add menu and the driver dropdown
        customDrivers = settingsHandler.loadCustomDrivers();

        // model is (re)built per selected connection: scoped to its driver family for
        // built-in drivers, or to the registered drivers for a registered connection
        driverTypeComboBox.addItemListener(e -> {
            if (loading || e.getStateChange() != ItemEvent.SELECTED) {
                return;
            }
            onDriverChanged(e.getItem());
        });
        downloadDriverLink.setListener((source, data) -> doDownloadDriver(), null);
        driverTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DriverTypeEnum item) {
                    setText(item.getDisplayName());
                    if (item.getIcon() != null) {
                        setIcon(MybatisIcons.load(item.getIcon()));
                    }
                } else if (value instanceof CustomDriverInfo item) {
                    setText(item.getName());
                    setIcon(MybatisIcons.CONNECTION);
                }
                return this;
            }
        });

        // test connection button
        testConnectionButton.addActionListener(e -> doTest());

        // connection list toolbar
        initConnectionListToolbar();

        // connection properties table
        initPropertiesTable();

        // history size
        SpinnerNumberModel model = (SpinnerNumberModel) historySizeSpinner.getModel();
        model.setMinimum(0);
        model.setMaximum(100);

        // clear all history
        clearAllButton.addActionListener(e -> settingsHandler.clearHistory());

        baseline = snapshot();
        installDirtyListeners(contentPanel);
        refreshDirty();
    }

    private void initConnectionListToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AddConnectionActionGroup());
        group.add(new RemoveConnectionAction());
        group.add(new MoveUpConnectionAction());
        group.add(new MoveDownConnectionAction());

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("MybatisBuilder.ConnectionList", group, true);
        toolbar.setTargetComponent(listPanel);

        // listPanel is a BorderLayout in the .form with scrollPane at CENTER;
        // dock the toolbar at the bottom.
        listPanel.add(toolbar.getComponent(), BorderLayout.NORTH);
    }

    private static final String[] PROPERTY_FIELD_NAMES = {"name", "value"};
    private static final String[] PROPERTY_COLUMN_NAMES = {"Property", "Value"};

    private void initPropertiesTable() {
        ObjectTableModel<PropertyEntry> model =
                new ObjectTableModel<>(new ArrayList<>(), PROPERTY_FIELD_NAMES, PROPERTY_COLUMN_NAMES);
        model.setEditableFieldNames(PROPERTY_FIELD_NAMES);
        propertiesTable.setModel(model);
        model.addTableModelListener(e -> refreshDirty());

        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AddPropertyAction());
        group.add(new RemovePropertyAction());

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("MybatisBuilder.ConnectionProperties", group, true);
        toolbar.setTargetComponent(propertiesPanel);

        // propertiesPanel comes from the .form with GridLayoutManager; switch to
        // BorderLayout so the toolbar can dock below the scroll pane.
        propertiesPanel.remove(propertiesScrollPane);
        propertiesPanel.setLayout(new BorderLayout());
        propertiesPanel.add(propertiesScrollPane, BorderLayout.CENTER);
        propertiesPanel.add(toolbar.getComponent(), BorderLayout.SOUTH);
    }

    private class AddPropertyAction extends AnAction {
        AddPropertyAction() {
            super("Add", "Add a property", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            stopEditing(propertiesTable);
            ObjectTableModel<PropertyEntry> model = propertiesModel();
            model.getItems().add(new PropertyEntry("", ""));
            int row = model.getItems().size() - 1;
            model.fireTableRowsInserted(row, row);
            propertiesTable.editCellAt(row, 0);
            propertiesTable.getEditorComponent().requestFocus();
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private class RemovePropertyAction extends AnAction {
        RemovePropertyAction() {
            super("Remove", "Remove the selected property", AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            stopEditing(propertiesTable);
            int row = propertiesTable.getSelectedRow();
            if (row < 0) {
                return;
            }
            ObjectTableModel<PropertyEntry> model = propertiesModel();
            model.getItems().remove(row);
            model.fireTableRowsDeleted(row, row);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(propertiesTable.getSelectedRow() >= 0);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    @SuppressWarnings("unchecked")
    private ObjectTableModel<PropertyEntry> propertiesModel() {
        return (ObjectTableModel<PropertyEntry>) propertiesTable.getModel();
    }

    private static void stopEditing(JTable table) {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private void showEmptyState(boolean empty) {
        CardLayout layout = (CardLayout) connectionContainer.getLayout();
        layout.show(connectionContainer, empty ? "empty" : "form");
        if (empty) {
            current = null;
            connectionEmptyLabel.setForeground(UIUtil.getInactiveTextColor());
        }
    }

    @SuppressWarnings("unchecked")
    private SettingsSnapshot snapshot() {
        if (current != null) {
            getData(current);
        }
        DefaultListModel<ConnectionInfo> model =
                (DefaultListModel<ConnectionInfo>) connectionList.getModel();
        List<ConnectionInfo> list = new ArrayList<>(model.size());
        for (int i = 0; i < model.size(); i++) {
            list.add(model.get(i));
        }
        DefaultParameters def = new DefaultParameters();
        getData(def);
        Map<String, Object> bundle = new LinkedHashMap<>();
        bundle.put("connections", list);
        bundle.put("defaults", def);
        String json = JsonUtil.toJson(bundle);
        if (json == null) {
            // Serialisation failure shouldn't break the dialog — fall back to a unique
            // marker so dirty stays asserted and the user can still save.
            return new SettingsSnapshot("ERR:" + System.nanoTime());
        }
        return new SettingsSnapshot(json);
    }

    private void refreshDirty() {
        if (loading) {
            return;
        }
        boolean dirty = snapshot().differsFrom(baseline);
        getApplyAction().setEnabled(dirty);
    }

    private void installDirtyListeners(JComponent root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPasswordField pf) {
                pf.getDocument().addDocumentListener(textDirtyListener);
            } else if (c instanceof JTextField tf) {
                tf.getDocument().addDocumentListener(textDirtyListener);
            } else if (c instanceof JComboBox<?> cb) {
                cb.addItemListener(itemDirtyListener);
            } else if (c instanceof JCheckBox cbx) {
                cbx.addItemListener(itemDirtyListener);
            } else if (c instanceof JSpinner sp) {
                sp.addChangeListener(changeDirtyListener);
            }
            if (c instanceof JComponent jc) {
                installDirtyListeners(jc);
            }
        }
    }

    private class NewConnectionAction extends AnAction {
        private final DriverTypeEnum type;

        NewConnectionAction(DriverTypeEnum type) {
            super(type.getDisplayName(),
                    "Add a new " + type.getDisplayName() + " connection",
                    MybatisIcons.load(type.getIcon()));
            this.type = type;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            doAdd(connectionList, type);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    /**
     * Popup "Add" group: built-in drivers grouped by family, then the registered
     * drivers, then "Register driver…". Children are rebuilt on each open so newly
     * registered drivers show up immediately.
     */
    private class AddConnectionActionGroup extends ActionGroup {
        AddConnectionActionGroup() {
            super("Add", true);
            getTemplatePresentation().setIcon(AllIcons.General.Add);
        }

        @Override
        public AnAction[] getChildren(@Nullable AnActionEvent e) {
            List<AnAction> actions = new ArrayList<>();
            Map<String, List<DriverTypeEnum>> byFamily = new LinkedHashMap<>();
            for (DriverTypeEnum type : DriverTypeEnum.values()) {
                byFamily.computeIfAbsent(type.getFamily(), k -> new ArrayList<>()).add(type);
            }
            for (Map.Entry<String, List<DriverTypeEnum>> entry : byFamily.entrySet()) {
                List<DriverTypeEnum> drivers = entry.getValue();
                if (drivers.size() == 1) {
                    actions.add(new NewConnectionAction(drivers.get(0)));
                } else {
                    DefaultActionGroup familyGroup = new DefaultActionGroup(entry.getKey(), true);
                    familyGroup.getTemplatePresentation().setIcon(MybatisIcons.load(drivers.get(0).getIcon()));
                    for (DriverTypeEnum type : drivers) {
                        familyGroup.add(new NewConnectionAction(type));
                    }
                    actions.add(familyGroup);
                }
            }
            if (!customDrivers.isEmpty()) {
                actions.add(Separator.getInstance());
                for (CustomDriverInfo driver : customDrivers) {
                    actions.add(new NewCustomConnectionAction(driver));
                }
            }
            actions.add(Separator.getInstance());
            actions.add(new RegisterDriverAction());
            return actions.toArray(AnAction.EMPTY_ARRAY);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private class NewCustomConnectionAction extends AnAction {
        private final CustomDriverInfo driver;

        NewCustomConnectionAction(CustomDriverInfo driver) {
            super(driver.getName(), "Add a new " + driver.getName() + " connection", MybatisIcons.CONNECTION);
            this.driver = driver;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            doAddCustom(driver);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private class RegisterDriverAction extends AnAction {
        RegisterDriverAction() {
            super("Register Driver…", "Register a custom JDBC driver", AllIcons.General.Settings);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            doRegisterDrivers();
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private class RemoveConnectionAction extends AnAction {
        RemoveConnectionAction() {
            super("Remove", "Remove the selected connection", AllIcons.General.Remove);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            doRemove(connectionList);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(connectionList.getSelectedIndex() >= 0);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private class MoveUpConnectionAction extends AnAction {
        MoveUpConnectionAction() {
            super("Move Up", "Move the selected connection up", AllIcons.Actions.MoveUp);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            doMove(connectionList, -1);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(connectionList.getSelectedIndex() > 0);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private class MoveDownConnectionAction extends AnAction {
        MoveDownConnectionAction() {
            super("Move Down", "Move the selected connection down", AllIcons.Actions.MoveDown);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            doMove(connectionList, 1);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            int idx = connectionList.getSelectedIndex();
            e.getPresentation().setEnabled(idx >= 0 && idx < connectionList.getModel().getSize() - 1);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
        }
    }

    private void initDefaultParameterPane() {
        // init component
        defaultModelTypeComboBox.setModel(new DefaultComboBoxModel(ModelType.values()));

        // set data
        DefaultParameters defaultParameters = settingsHandler.getDefaultParameters();
        setData(defaultParameters);
    }

    private void doMove(JList list, int i) {
        int fromIndex = list.getSelectedIndex();
        if (fromIndex < 0) {
            return;
        }

        // check bound
        int toIndex = fromIndex + i;
        DefaultListModel model = (DefaultListModel) list.getModel();
        if (toIndex < 0 || toIndex > model.getSize() - 1) {
            return;
        }

        // swap
        Object from = model.getElementAt(fromIndex);
        Object to = model.getElementAt(toIndex);
        model.setElementAt(from, toIndex);
        model.setElementAt(to, fromIndex);
        list.setSelectedIndex(toIndex);
    }

    private void doTest() {
        getData(current);
        DriverTypeEnum type = current.getDriverType();
        if (type != null && type.isDownloadable() && !DriverDownloader.getInstance().isPresent(type)) {
            Messages.showWarningDialog(project,
                    "The " + type.getDisplayName() + " driver has not been downloaded yet. Click \"Download driver\" first.",
                    "Driver Required");
            return;
        }
        settingsHandler.testConnection(current);
    }

    private void onDriverChanged(Object item) {
        if (item instanceof DriverTypeEnum type) {
            onDriverTypeChanged(type);
        } else if (item instanceof CustomDriverInfo driver) {
            onCustomDriverChanged(driver);
        }
    }

    /**
     * Re-fill defaults and toggle field visibility when the built-in driver changes.
     */
    private void onDriverTypeChanged(DriverTypeEnum type) {
        if (type == null) {
            return;
        }
        applyLayout(type.getLayout());
        Integer defaultPort = type.getDefaultPort();
        if (defaultPort != null && defaultPort > 0) {
            portSpinner.setValue(defaultPort);
        }
        // built-in / managed driver: clear any leftover overrides so the resolved
        // driver class and url pattern are used instead.
        urlText.setText("");
        driverClassText.setText("");
        driverLibraryText.setText("");
        setDriverPanelEditable(true);
        driverPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Override (optional)"));
        refreshDriverStatus(type);
    }

    /**
     * Re-fill defaults when switching to another registered driver.
     */
    private void onCustomDriverChanged(CustomDriverInfo driver) {
        applyLayout(DriverTypeEnum.Layout.HOST);
        if (driver.getDefaultPort() != null && driver.getDefaultPort() > 0) {
            portSpinner.setValue(driver.getDefaultPort());
        }
        // url is built from the registered driver's template + host/port/db
        urlText.setText("");
        driverClassText.setText(nullToEmpty(driver.getDriverClass()));
        driverLibraryText.setText(nullToEmpty(driver.getDriverLibrary()));
        setDriverPanelEditable(false);
        driverPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Driver Info"));
        refreshCustomDriverStatus(driver);
    }

    private void setDriverPanelEditable(boolean editable) {
        driverClassText.setEnabled(editable);
        driverLibraryText.setEnabled(editable);
        driverLibraryText.getButton().setEnabled(editable);
        urlText.setEnabled(editable);
    }

    /**
     * Rebuild the driver dropdown so it lists only the drivers of the given family.
     */
    @SuppressWarnings("unchecked")
    private void rebuildDriverCombo(String family) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (DriverTypeEnum t : DriverTypeEnum.values()) {
            if (t.getFamily().equals(family)) {
                model.addElement(t);
            }
        }
        driverTypeComboBox.setModel(model);
    }

    /**
     * Rebuild the driver dropdown to list the registered drivers (for a custom connection).
     */
    @SuppressWarnings("unchecked")
    private void rebuildCustomDriverCombo() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (CustomDriverInfo driver : customDrivers) {
            model.addElement(driver);
        }
        driverTypeComboBox.setModel(model);
    }

    private CustomDriverInfo findRegisteredDriver(String id) {
        if (id == null) {
            return null;
        }
        for (CustomDriverInfo driver : customDrivers) {
            if (id.equals(driver.getId())) {
                return driver;
            }
        }
        return null;
    }

    /**
     * Show host/port or file-path fields depending on the layout.
     */
    private void applyLayout(DriverTypeEnum.Layout layout) {
        boolean host = layout == DriverTypeEnum.Layout.HOST;
        boolean file = layout == DriverTypeEnum.Layout.FILE;

        hostLabel.setVisible(host);
        hostText.setVisible(host);
        portLabel.setVisible(host);
        portSpinner.setVisible(host);
        databaseLabel.setText(file ? "File" : "Database");
    }

    private void refreshDriverStatus(DriverTypeEnum type) {
        DriverDownloader downloader = DriverDownloader.getInstance();
        if (type.isBundled()) {
            driverStatusLabel.setText("Driver: bundled");
            downloadDriverLink.setVisible(false);
        } else { // downloadable
            if (downloader.isPresent(type)) {
                driverStatusLabel.setText("Driver: " + downloader.jarName(type));
                downloadDriverLink.setText("Re-download");
            } else {
                driverStatusLabel.setText("Driver not downloaded");
                downloadDriverLink.setText("Download driver");
            }
            downloadDriverLink.setVisible(true);
        }
    }

    private void refreshCustomDriverStatus(CustomDriverInfo driver) {
        String library = driver == null ? null : driver.getDriverLibrary();
        if (StringUtil.stringHasValue(library)) {
            driverStatusLabel.setText("Driver library: " + new java.io.File(library).getName());
        } else {
            driverStatusLabel.setText("Registered driver — no library set");
        }
        downloadDriverLink.setVisible(false);
    }

    private void doDownloadDriver() {
        Object selected = driverTypeComboBox.getSelectedItem();
        if (!(selected instanceof DriverTypeEnum) || !((DriverTypeEnum) selected).isDownloadable()) {
            return;
        }
        DriverTypeEnum type = (DriverTypeEnum) selected;
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Downloading " + type.getDisplayName() + " Driver", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    DriverDownloader.getInstance().download(type, indicator);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

            @Override
            public void onSuccess() {
                // only refresh if the user is still on the same type
                if (type == driverTypeComboBox.getSelectedItem()) {
                    refreshDriverStatus(type);
                }
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                Messages.showErrorDialog(project, error.getMessage(), "Driver Download Failed");
            }
        });
    }

    private void doRegisterDrivers() {
        RegisterDriverDialog dialog = new RegisterDriverDialog(project, customDrivers);
        if (dialog.showAndGet()) {
            customDrivers = dialog.getDrivers();
            settingsHandler.saveCustomDrivers(customDrivers);
            // reflect any edits (driver class / library / url) in the current connection view
            if (current != null && current.getDriverType() == null) {
                setData(current);
            }
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void doSelect(JList list) {
        ConnectionInfo selected = (ConnectionInfo) list.getSelectedValue();
        if (selected == null) {
            return;
        }
        showEmptyState(false);

        // before change, save previous item
        if (current != null) {
            getData(current);
        }

        // Update current BEFORE setData so the final refreshDirty inside setData
        // snapshots into the new selection rather than overwriting the previous one.
        current = selected;
        setData(selected);
    }

    private void doRemove(JList list) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }

        DefaultListModel model = (DefaultListModel) list.getModel();
        model.remove(selectedIndex);

        if (model.getSize() == 0) {
            showEmptyState(true);
            return;
        }

        // re-select
        if (selectedIndex > model.getSize() - 1) {
            selectedIndex = model.getSize() - 1;
        }
        list.setSelectedIndex(selectedIndex);
    }

    private void doAdd(JList list, DriverTypeEnum type) {
        ConnectionInfo blank = new ConnectionInfo();
        blank.setId(UUID.randomUUID().toString().replace("-", ""));
        blank.setName("unnamed " + type.getDisplayName());
        blank.setDriverType(type);
        blank.setPort(type.getDefaultPort());
        blank.setProperties(new LinkedHashMap<>(type.getDefaultProperties()));

        DefaultListModel model = (DefaultListModel) list.getModel();
        model.addElement(blank);
        list.setSelectedIndex(model.getSize() - 1);
    }

    private void doAddCustom(CustomDriverInfo driver) {
        ConnectionInfo blank = new ConnectionInfo();
        blank.setId(UUID.randomUUID().toString().replace("-", ""));
        blank.setName("unnamed " + driver.getName());
        // registered driver: driverType is null, snapshot the driver definition
        blank.setDriverType(null);
        blank.setCustomDriverId(driver.getId());
        blank.setDriverClass(driver.getDriverClass());
        blank.setDriverLibrary(driver.getDriverLibrary());
        blank.setUrlPattern(driver.getUrlPattern());
        if (driver.getDefaultPort() != null) {
            blank.setPort(driver.getDefaultPort());
        }
        blank.setProperties(new LinkedHashMap<>());

        DefaultListModel model = (DefaultListModel) connectionList.getModel();
        model.addElement(blank);
        connectionList.setSelectedIndex(model.getSize() - 1);
    }

    private void saveAll() {
        if (current != null) {
            getData(current);
        }

        DefaultListModel model = (DefaultListModel) connectionList.getModel();
        List<ConnectionInfo> list = new ArrayList<>(model.size());
        for (int i = 0; i < model.getSize(); i++) {
            list.add((ConnectionInfo) model.getElementAt(i));
        }

        DefaultParameters defaultParameters = new DefaultParameters();
        getData(defaultParameters);

        settingsHandler.saveAll(list, defaultParameters);
    }

    @Override
    protected void doOKAction() {
        saveAll();
        super.doOKAction();
    }

    protected void doApplyAction(ActionEvent e) {
        // simulate OK button but not close window
        saveAll();

        baseline = snapshot();
        refreshDirty();
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getApplyAction(),
                getOKAction(),
                getHelpAction()
        };
    }

    private Action getApplyAction() {
        if (applyAction == null) {
            applyAction = new AbstractAction("Apply") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doApplyAction(e);
                }
            };
        }

        return applyAction;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    public void setData(DefaultParameters defaultParameters) {
        if (defaultParameters.getDefaultModelType() == null) {
            defaultModelTypeComboBox.setSelectedIndex(0);
        } else {
            defaultModelTypeComboBox.setSelectedItem(defaultParameters.getDefaultModelType());
        }

        javaFileEncodingText.setText(defaultParameters.getJavaFileEncoding());

        generatedCommentText.setText(defaultParameters.getGeneratedComment());

        forceBigDecimalsCheckbox.setSelected(defaultParameters.getForceBigDecimals());
        useJSR310TypesCheckBox.setSelected(defaultParameters.getUseJSR310Types());

        historySizeSpinner.setValue(defaultParameters.getHistorySize());

        // plugins
        customAnnotationTypeText.setText(defaultParameters.getMapperAnnotationConfig().customAnnotationType);

        SelectWithLockConfig selectWithLockConfig = defaultParameters.getSelectWithLockConfig();
        byPrimaryKeyOverrideText.setText(selectWithLockConfig.byPrimaryKeyWithLockOverride);
        byExampleOverrideText.setText(selectWithLockConfig.byExampleWithLockOverride);

        RenamePlugin.Config renameConfig = defaultParameters.getRenameConfig();
        mapperTypePatternText.setText(renameConfig.mapperTypePattern);
        exampleTypePatternText.setText(renameConfig.exampleTypePattern);
        sqlFileNamePatternText.setText(renameConfig.sqlFileNamePattern);
    }

    public void getData(DefaultParameters defaultParameters) {
        defaultParameters.setDefaultModelType((ModelType) defaultModelTypeComboBox.getSelectedItem());
        defaultParameters.setJavaFileEncoding(javaFileEncodingText.getText());
        defaultParameters.setGeneratedComment(generatedCommentText.getText());
        defaultParameters.setForceBigDecimals(forceBigDecimalsCheckbox.isSelected());
        defaultParameters.setUseJSR310Types(useJSR310TypesCheckBox.isSelected());
        defaultParameters.setHistorySize((Integer) historySizeSpinner.getValue());

        // plugins
        defaultParameters.getMapperAnnotationConfig().customAnnotationType = customAnnotationTypeText.getText();

        SelectWithLockConfig selectWithLockConfig = defaultParameters.getSelectWithLockConfig();
        selectWithLockConfig.byPrimaryKeyWithLockOverride = byPrimaryKeyOverrideText.getText();
        selectWithLockConfig.byExampleWithLockOverride = byExampleOverrideText.getText();

        RenamePlugin.Config renameConfig = defaultParameters.getRenameConfig();
        renameConfig.mapperTypePattern = mapperTypePatternText.getText();
        renameConfig.exampleTypePattern = exampleTypePatternText.getText();
        renameConfig.sqlFileNamePattern = sqlFileNamePatternText.getText();
    }

    public void setData(ConnectionInfo data) {
        loading = true;
        try {
            doSetData(data);
        } finally {
            loading = false;
        }
        refreshDirty();
    }

    private void doSetData(ConnectionInfo data) {
        ViewUtil.makeAvailable(connectionPanel, true);

        connectionNameText.setText(data.getName());
        descriptionText.setText(data.getDescription());

        DriverTypeEnum type = data.getDriverType();
        if (type != null) {
            // built-in driver: scope the dropdown to the family (e.g. MySQL 5 / 8 / MariaDB)
            rebuildDriverCombo(type.getFamily());
            driverTypeComboBox.setSelectedItem(type);
            applyLayout(type.getLayout());
            refreshDriverStatus(type);
            setDriverPanelEditable(true);
            driverPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Override (optional)"));
        } else {
            // registered driver: re-sync the snapshot from the (possibly edited) registry
            CustomDriverInfo driver = findRegisteredDriver(data.getCustomDriverId());
            if (driver != null) {
                data.setDriverClass(driver.getDriverClass());
                data.setDriverLibrary(driver.getDriverLibrary());
                data.setUrlPattern(driver.getUrlPattern());
            }
            rebuildCustomDriverCombo();
            driverTypeComboBox.setSelectedItem(driver); // null -> no selection (dangling)
            applyLayout(DriverTypeEnum.Layout.HOST);
            refreshCustomDriverStatus(driver);
            setDriverPanelEditable(false);
            driverPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Driver Info"));
        }

        driverLibraryText.setText(data.getDriverLibrary());
        driverClassText.setText(data.getDriverClass());
        urlText.setText(data.getUrl());

        hostText.setText(data.getHost());
        if (data.getPort() != null) {
            portSpinner.setValue(data.getPort());
        }

        userText.setText(data.getUserName());
        passwordText.setText(data.getPassword());
        databaseText.setText(data.getDatabase());

        activeCheckBox.setSelected(data.getActive());

        // properties table
        ObjectTableModel<PropertyEntry> propsModel = propertiesModel();
        propsModel.getItems().clear();
        if (data.getProperties() != null) {
            for (Map.Entry<String, String> e : data.getProperties().entrySet()) {
                propsModel.getItems().add(new PropertyEntry(e.getKey(), e.getValue()));
            }
        }
        propsModel.fireTableDataChanged();

        testConnectionButton.setEnabled(true);
    }

    public void getData(ConnectionInfo data) {
        data.setName(connectionNameText.getText());
        data.setDescription(descriptionText.getText());

        Object selected = driverTypeComboBox.getSelectedItem();
        if (selected instanceof DriverTypeEnum type) {
            data.setDriverType(type);
            data.setCustomDriverId(null);
            data.setUrlPattern(null);
            data.setDriverLibrary(driverLibraryText.getText());
            data.setDriverClass(driverClassText.getText());
            data.setUrl(urlText.getText());
        } else if (selected instanceof CustomDriverInfo driver) {
            // registered driver: snapshot its definition onto the connection
            data.setDriverType(null);
            data.setCustomDriverId(driver.getId());
            data.setDriverClass(driver.getDriverClass());
            data.setDriverLibrary(driver.getDriverLibrary());
            data.setUrlPattern(driver.getUrlPattern());
        } else {
            // no selection (dangling registered driver): keep the existing snapshot
            data.setDriverType(null);
        }

        data.setHost(hostText.getText());
        data.setPort((Integer) portSpinner.getValue());
        data.setUserName(userText.getText());
        data.setPassword(String.valueOf(passwordText.getPassword()));
        data.setDatabase(databaseText.getText());

        data.setActive(activeCheckBox.isSelected());

        stopEditing(propertiesTable);
        Map<String, String> propsMap = new LinkedHashMap<>();
        for (PropertyEntry entry : propertiesModel().getItems()) {
            String key = entry.getName() == null ? "" : entry.getName().trim();
            if (key.isEmpty()) {
                continue;
            }
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            propsMap.put(key, value); // last write wins on duplicate key
        }
        data.setProperties(propsMap);
    }

    protected ValidationInfo doValidate() {
        ValidationInfo info = renameValidate(mapperTypePatternText, exampleTypePatternText, sqlFileNamePatternText);

        if (info != null) {
            ViewUtil.focusTab(info.component);
        }

        return info;
    }

    @Nullable
    private ValidationInfo renameValidate(JTextField... textFields) {
        ValidationInfo info = null;
        for (JTextField textField : textFields) {
            String pattern = textField.getText();
            if (StringUtil.stringHasValue(pattern)) {
                if (!pattern.contains(RenamePlugin.DOMAIN_NAME)) {
                    info = new ValidationInfo("Pattern should contain " + RenamePlugin.DOMAIN_NAME, textField);
                    break;
                }
            }
        }

        return info;
    }

    @Override // remember window position and size
    protected String getDimensionServiceKey() {
        return "MyBatisBuilder.SettingsDialog";
    }

    @Override
    protected String getHelpId() {
        return "https://mybatis.chuntung.com";
    }

    @Override
    protected void doHelpAction() {
        if (myHelpAction.isEnabled()) {
            BrowserUtil.browse(getHelpId());
        }
    }

    private void createUIComponents() {
        // place custom component creation code here
        urlLabel = new LinkLabel<>("URL", AllIcons.Ide.External_link_arrow, (aSource, aLinkData) -> BrowserUtil.browse("https://chuntung.com/jdbc-url"));
        urlLabel.setToolTipText("Click to view URL syntax for common databases");

        downloadDriverLink = new LinkLabel<>("Download driver", AllIcons.Actions.Download);
    }
}
