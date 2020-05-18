/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.action.SettingsHandler;
import com.chuntung.plugin.mybatis.builder.generator.plugins.RenamePlugin;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DriverTypeEnum;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.chuntung.plugin.mybatis.builder.util.ViewUtil;
import com.chuntung.plugin.mybatis.builder.generator.DefaultParameters;
import com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock.SelectWithLockConfig;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.components.labels.LinkLabel;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.config.ModelType;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MybatisBuilderSettingsDialog extends DialogWrapper {
    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, false, false, false);
    private JPanel contentPanel;
    private JList connectionList;
    private JButton addButton;
    private JButton removeButton;
    private JButton downButton;
    private JButton upButton;
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
    private JComboBox targetRuntimeComboBox;
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

    private final SettingsHandler settingsHandler;
    private Project project;
    private ConnectionInfo current;
    private Action applyAction;

    private static final String[] targetRuntimes = {"MyBatis3", "MyBatis3Simple"};

    public MybatisBuilderSettingsDialog(@Nullable Project project) {
        super(project);
        this.project = project;
        settingsHandler = SettingsHandler.getInstance(project);

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
        connectionList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                ConnectionInfo item = (ConnectionInfo) value;
                return super.getListCellRendererComponent(list, item.getName(), index, isSelected, cellHasFocus);
            }
        });
        connectionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                doSelect((JList) e.getSource());
            }
        });

        // view panel
        ViewUtil.makeAvailable(connectionPanel, false);

        driverPanel.setVisible(false);
        driverLibraryText.addBrowseFolderListener("Choose Library", "Library should contain java.sql.Driver implement ", null, LIBRARY_FILE_DESCRIPTOR);

        portSpinner.setModel(new SpinnerNumberModel(3306, 80, 65536, 1));
        portSpinner.setEditor(new JSpinner.NumberEditor(portSpinner, "#"));

        driverTypeComboBox.setModel(new DefaultComboBoxModel(DriverTypeEnum.values()));
        driverTypeComboBox.addItemListener(e -> {
            DriverTypeEnum item = (DriverTypeEnum) e.getItem();
            driverPanel.setVisible(DriverTypeEnum.Custom.equals(item));
            hostPanel.setVisible(!DriverTypeEnum.Custom.equals(item));
            portSpinner.setValue(item.getDefaultPort());
        });
        driverTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                DriverTypeEnum item = (DriverTypeEnum) value;
                if (item.getIcon() != null) {
                    setIcon(IconLoader.getIcon(item.getIcon()));
                }
                return this;
            }
        });

        // test connection button
        testConnectionButton.addActionListener(e -> doTest());

        // add button
        addButton.addActionListener(e -> doAdd(connectionList));

        // remove button
        removeButton.addActionListener(e -> doRemove(connectionList));

        upButton.addActionListener(e -> doMove(connectionList, -1));

        downButton.addActionListener(e -> doMove(connectionList, 1));

        // history size
        SpinnerNumberModel model = (SpinnerNumberModel) historySizeSpinner.getModel();
        model.setMinimum(0);
        model.setMaximum(100);

        // clear all history
        clearAllButton.addActionListener(e -> settingsHandler.clearHistory());
    }

    private void initDefaultParameterPane() {
        // init component
        defaultModelTypeComboBox.setModel(new DefaultComboBoxModel(ModelType.values()));
        targetRuntimeComboBox.setModel(new DefaultComboBoxModel(targetRuntimes));

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
        settingsHandler.testConnection(current);
    }

    private void doSelect(JList list) {
        ConnectionInfo selected = (ConnectionInfo) list.getSelectedValue();
        if (selected == null) {
            return;
        }

        // before change, save previous item
        if (current != null) {
            getData(current);
        }

        // change current to new item
        setData(selected);
        current = selected;
    }

    private void doRemove(JList list) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }

        DefaultListModel model = (DefaultListModel) list.getModel();
        model.remove(selectedIndex);

        // re-select
        if (selectedIndex > model.getSize() - 1) {
            selectedIndex = model.getSize() - 1;
        }
        list.setSelectedIndex(selectedIndex);
    }

    private void doAdd(JList list) {
        ConnectionInfo blank = new ConnectionInfo();
        blank.setId(UUID.randomUUID().toString().replace("-", ""));
        blank.setName("unnamed");

        DefaultListModel model = (DefaultListModel) list.getModel();
        model.addElement(blank);
        list.setSelectedIndex(model.getSize() - 1);
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
        getOKAction().setEnabled(false);
        getOKAction().actionPerformed(e);
        getOKAction().setEnabled(true);

        getApplyAction().setEnabled(false);
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
        if (defaultParameters.getTargetRuntime() == null) {
            targetRuntimeComboBox.setSelectedIndex(0);
        } else {
            targetRuntimeComboBox.setSelectedItem(defaultParameters.getTargetRuntime());
        }

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
        defaultParameters.setTargetRuntime((String) targetRuntimeComboBox.getSelectedItem());
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
        ViewUtil.makeAvailable(connectionPanel, true);

        connectionNameText.setText(data.getName());
        descriptionText.setText(data.getDescription());
        if (data.getDriverType() != null) {
            driverTypeComboBox.setSelectedItem(data.getDriverType());
        } else {
            driverTypeComboBox.setSelectedIndex(0);
        }

        driverPanel.setVisible(DriverTypeEnum.Custom.equals(data.getDriverType()));
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

        testConnectionButton.setEnabled(true);
        applyAction.setEnabled(true);
    }

    public void getData(ConnectionInfo data) {
        data.setName(connectionNameText.getText());
        data.setDescription(descriptionText.getText());

        data.setDriverType((DriverTypeEnum) driverTypeComboBox.getSelectedItem());
        data.setDriverLibrary(driverLibraryText.getText());
        data.setDriverClass(driverClassText.getText());
        data.setUrl(urlText.getText());

        data.setHost(hostText.getText());
        data.setPort((Integer) portSpinner.getValue());
        data.setUserName(userText.getText());
        data.setPassword(String.valueOf(passwordText.getPassword()));
        data.setDatabase(databaseText.getText());

        data.setActive(activeCheckBox.isSelected());
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
        urlLabel = LinkLabel.create("URL", () -> BrowserUtil.browse("https://chuntung.com/jdbc-url"));
        urlLabel.setToolTipText("Click to view URL syntax for common databases");
    }
}
