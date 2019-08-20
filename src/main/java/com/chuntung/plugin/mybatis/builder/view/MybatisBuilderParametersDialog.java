/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.action.SettingsHandler;
import com.chuntung.plugin.mybatis.builder.generator.*;
import com.chuntung.plugin.mybatis.builder.generator.plugins.LombokPlugin;
import com.chuntung.plugin.mybatis.builder.generator.plugins.MapperAnnotationPlugin;
import com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock.SelectWithLockConfig;
import com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock.SelectWithLockPlugin;
import com.chuntung.plugin.mybatis.builder.model.HistoryCategoryEnum;
import com.chuntung.plugin.mybatis.builder.model.ObjectTableModel;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.chuntung.plugin.mybatis.builder.util.ConfigUtil;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.chuntung.plugin.mybatis.builder.util.ViewUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiPackage;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.internal.db.DatabaseDialects;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

public class MybatisBuilderParametersDialog extends DialogWrapper {
    private static final FileChooserDescriptor FOLDER_DESCRIPTOR = new FileChooserDescriptor(false, true, false, false, false, false);
    private JPanel mainPanel;
    private TextFieldWithHistoryWithBrowseButton javaModelPackageText;
    private TextFieldWithBrowseButton javaModelProjectText;
    private JCheckBox trimStringsCheckBox;
    private JPanel sqlMapGeneratorPanel;
    private TextFieldWithHistoryWithBrowseButton sqlMapPackageText;
    private TextFieldWithBrowseButton sqlMapProjectText;
    private TextFieldWithHistoryWithBrowseButton javaClientPackageText;
    private TextFieldWithBrowseButton javaClientProjectText;
    private JComboBox javaClientTypeComboBox;
    private JCheckBox countByExampleCheckbox;
    private JCheckBox updateByExampleCheckbox;
    private JCheckBox deleteByExampleCheckbox;
    private JCheckBox selectByExampleCheckbox;
    private JTable selectedTables;
    private JTextField endingDelimiterText;
    private JTextField beginningDelimiterText;
    private JCheckBox mapperAnnotationSupportCheckBox;
    private JTextField columnText;
    private JComboBox statementComboBox;
    private JCheckBox exampleAllCheckBox;
    private JCheckBox lombokSupportCheckBox;
    private JCheckBox selectByPrimaryKeyWithLockCheckBox;
    private JCheckBox databaseRemarkCheckBox;
    private JCheckBox identityCheckBox;
    private JComboBox keyTypeComboBox;
    private JCheckBox basicAllCheckBox;
    private JCheckBox insertCheckBox;
    private JCheckBox updateByPrimaryKeyCheckBox;
    private JCheckBox selectByPrimaryKeyCheckBox;
    private JCheckBox deleteByPrimaryKeyCheckBox;
    private JCheckBox selectByExampleWithLockCheckBox;
    private JLabel basicStatementLabel;
    private JPanel basicPanel;
    private JPanel examplePanel;
    private JCheckBox lockAllCheckBox;
    private JPanel lockPanel;
    private JScrollPane selectedTablePanel;
    private JTextField domainSearchText;
    private JTextField domainReplaceText;
    private JButton replaceButton;
    private JButton resetButton;
    private boolean morePanelVisible = false;

    private String[] javaClientTypes = {"XMLMAPPER", "ANNOTATEDMAPPER", "MIXEDMAPPER"};
    private String[] keyTypes = {"", "pre", "post"};

    private String[] fieldNames = new String[]{"tableName", "domainName", "keyColumn", ""};
    private String[] editableFieldNames = new String[]{"domainName", "keyColumn", ""};
    private String[] columnNames = new String[]{"Table name", "Domain name", "Key column", "Columns setting"};

    private Project project;
    private String connectionId;
    private GeneratorParamWrapper paramWrapper;
    private final SettingsHandler settingsHandler;

    private class TableButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor {
        JPanel panel = new JPanel();

        TableButtonRenderer() {
            JButton button = new JButton(new AbstractAction("Open") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TableButtonRenderer.this.fireEditingCanceled();
                    ObjectTableModel<TableInfo> model = (ObjectTableModel<TableInfo>) selectedTables.getModel();
                    int selected = selectedTables.getSelectedRow();
                    TableInfo tableInfo = model.getItems().get(selected);
                    new ColumnsSettingDialog(connectionId, tableInfo, project).show();
                }
            });
            panel.setLayout(new BorderLayout());
            panel.add(button);
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
    }

    public MybatisBuilderParametersDialog(Project project, GeneratorParamWrapper paramWrapper, String connectionId) {
        super(project);
        this.project = project;
        this.paramWrapper = paramWrapper;
        this.connectionId = connectionId;
        this.settingsHandler = SettingsHandler.getInstance(project);

        initGUI(project);
        setData(paramWrapper);

        init();
    }

    private void initGUI(Project project) {
        setTitle("MyBatis Builder - Parameters");
        Cursor hand = new Cursor(Cursor.HAND_CURSOR);

        // init checkbox panel
        ViewUtil.initCheckBoxPanel(examplePanel, exampleAllCheckBox);
        ViewUtil.initCheckBoxPanel(basicPanel, basicAllCheckBox);
        ViewUtil.initCheckBoxPanel(lockPanel, lockAllCheckBox);

        // generated key
        identityCheckBox.setCursor(hand);
        DefaultComboBoxModel statementModel = new DefaultComboBoxModel(DatabaseDialects.values());
        statementModel.insertElementAt("JDBC", 0);
        statementComboBox.setModel(statementModel);
        keyTypeComboBox.setModel(new DefaultComboBoxModel(keyTypes));

        // rename domain
        replaceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String regex = domainSearchText.getText(), replace = domainReplaceText.getText();
                ObjectTableModel<TableInfo> model = (ObjectTableModel<TableInfo>) selectedTables.getModel();
                for (TableInfo item : model.getItems()) {
                    if (item.getDomainName() != null && !item.getDomainName().isEmpty()) {
                        item.setDomainName(item.getDomainName().replaceAll(regex, replace));
                    }
                }
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ObjectTableModel<TableInfo> model = (ObjectTableModel<TableInfo>) selectedTables.getModel();
                for (TableInfo item : model.getItems()) {
                    item.setDomainName(JavaBeansUtil.getCamelCaseString(item.getTableName(), true));
                }
            }
        });

        // java client Combo box
        javaClientTypeComboBox.setModel(new DefaultComboBoxModel(javaClientTypes));
        javaClientTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                sqlMapGeneratorPanel.setVisible(!"ANNOTATEDMAPPER".equals(e.getItem()));
            }
        });

        // directory chooser
        javaModelProjectText.addBrowseFolderListener("Choose Target Project", "", null, FOLDER_DESCRIPTOR);
        javaClientProjectText.addBrowseFolderListener("Choose Target Project", "", null, FOLDER_DESCRIPTOR);
        sqlMapProjectText.addBrowseFolderListener("Choose Target Project", "", null, FOLDER_DESCRIPTOR);

        // package chooser
        javaModelPackageText.addActionListener(getPackageActionListener(project, javaModelPackageText));
        javaClientPackageText.addActionListener(getPackageActionListener(project, javaClientPackageText));
        sqlMapPackageText.addActionListener(getPackageActionListener(project, sqlMapPackageText));
    }

    @NotNull
    private ActionListener getPackageActionListener(Project project, TextFieldWithHistoryWithBrowseButton textField) {
        return e -> {
            PackageChooserDialog chooser = new PackageChooserDialog("Choose target package", project);
            chooser.selectPackage(textField.getText());
            chooser.show();
            PsiPackage selectedPackage = chooser.getSelectedPackage();
            if (selectedPackage != null) {
                textField.setTextAndAddToHistory(selectedPackage.getQualifiedName());
            }
        };
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return mainPanel;
    }

    public void setData(GeneratorParamWrapper data) {
        // context
        beginningDelimiterText.setText(data.getBeginningDelimiter());
        endingDelimiterText.setText(data.getEndingDelimiter());

        // model property
        trimStringsCheckBox.setSelected(data.getTrimStrings());
        databaseRemarkCheckBox.setSelected(data.getTrimStrings());

        // plugins
        DefaultParameters defaultParameters = settingsHandler.getDefaultParameters();

        boolean mapperAnnotationEnabled = data.getSelectedPlugins().containsKey(MapperAnnotationPlugin.class.getName());
        mapperAnnotationSupportCheckBox.setSelected(mapperAnnotationEnabled);
        String customAnnotationType = defaultParameters.getMapperAnnotationConfig().customAnnotationType;
        mapperAnnotationSupportCheckBox.setToolTipText(customAnnotationType);

        boolean lombokEnabled = data.getSelectedPlugins().containsKey(LombokPlugin.class.getName());
        lombokSupportCheckBox.setSelected(lombokEnabled);

        // -- select with lock
        String selectWithLockPlugin = SelectWithLockPlugin.class.getName();
        boolean selectWithLockEnabled = data.getSelectedPlugins().containsKey(selectWithLockPlugin);
        PluginConfigWrapper pluginConfigWrapper = data.getSelectedPlugins().get(selectWithLockPlugin);
        if (selectWithLockEnabled && pluginConfigWrapper != null) {
            SelectWithLockConfig selectWithLockConfig = (SelectWithLockConfig) pluginConfigWrapper.getPluginConfig();
            selectByPrimaryKeyWithLockCheckBox.setSelected(selectWithLockConfig.byPrimaryKeyWithLockEnabled);
            selectByExampleWithLockCheckBox.setSelected(selectWithLockConfig.byExampleWithLockEnabled);
        }
        SelectWithLockConfig selectWithLockConfig = defaultParameters.getSelectWithLockConfig();
        Object byPrimaryKeyOverride = ConfigUtil.getFieldValueByConfigKey(selectWithLockConfig, SelectWithLockConfig.BY_PRIMARY_KEY_WITH_LOCK_OVERRIDE);
        Object byExampleOverride = ConfigUtil.getFieldValueByConfigKey(selectWithLockConfig, SelectWithLockConfig.BY_EXAMPLE_WITH_LOCK_OVERRIDE);
        selectByPrimaryKeyWithLockCheckBox.setToolTipText(StringUtil.valueOf(byPrimaryKeyOverride));
        selectByExampleWithLockCheckBox.setToolTipText(StringUtil.valueOf(byExampleOverride));

        // default table config
        TableConfigurationWrapper defaultTableConfig = data.getDefaultTableConfigWrapper();
        insertCheckBox.setSelected(defaultTableConfig.isInsertStatementEnabled());
        updateByPrimaryKeyCheckBox.setSelected(defaultTableConfig.isUpdateByPrimaryKeyStatementEnabled());
        selectByPrimaryKeyCheckBox.setSelected(defaultTableConfig.isSelectByPrimaryKeyStatementEnabled());
        deleteByPrimaryKeyCheckBox.setSelected(defaultTableConfig.isDeleteByPrimaryKeyStatementEnabled());

        selectByExampleCheckbox.setSelected(defaultTableConfig.isSelectByExampleStatementEnabled());
        countByExampleCheckbox.setSelected(defaultTableConfig.isCountByExampleStatementEnabled());
        updateByExampleCheckbox.setSelected(defaultTableConfig.isUpdateByExampleStatementEnabled());
        deleteByExampleCheckbox.setSelected(defaultTableConfig.isDeleteByExampleStatementEnabled());

        domainSearchText.setText(defaultTableConfig.getDomainObjectRenamingRule().getSearchString());
        domainReplaceText.setText(defaultTableConfig.getDomainObjectRenamingRule().getReplaceString());

        GeneratedKeyWrapper generatedKeyWrapper = defaultTableConfig.getGeneratedKeyWrapper();
        columnText.setText(generatedKeyWrapper.getColumn());
        identityCheckBox.setSelected(generatedKeyWrapper.isIdentity());
        DatabaseDialects statement = DatabaseDialects.getDatabaseDialect(generatedKeyWrapper.getStatement());
        if (statement != null) {
            statementComboBox.setSelectedItem(statement);
        } else {
            statementComboBox.setSelectedItem(generatedKeyWrapper.getStatement());
        }
        keyTypeComboBox.setSelectedItem(generatedKeyWrapper.getType());

        // render all check box after set data
        ViewUtil.renderAllCheckBox(examplePanel, exampleAllCheckBox);
        ViewUtil.renderAllCheckBox(basicPanel, basicAllCheckBox);
        ViewUtil.renderAllCheckBox(lockPanel, lockAllCheckBox);

        // selected tables
        TitledBorder border = (TitledBorder) selectedTablePanel.getBorder();
        border.setTitle(border.getTitle() + ": " + data.getSelectedTables().size());

        ObjectTableModel<TableInfo> tableModel = new ObjectTableModel(data.getSelectedTables(), fieldNames, columnNames);
        tableModel.setEditableFieldNames(editableFieldNames);
        selectedTables.setModel(tableModel);
        selectedTables.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // set table button after data binding
        TableColumn column = selectedTables.getColumnModel().getColumn(3);
        TableButtonRenderer btnRenderer = new TableButtonRenderer();
        column.setCellRenderer(btnRenderer);
        column.setCellEditor(btnRenderer);

        List<String> history = null;
        // model
        JavaModelGeneratorConfiguration modelConfig = data.getJavaModelConfig();
        javaModelProjectText.setText(modelConfig.getTargetProject());

        history = paramWrapper.getHistoryMap().get(HistoryCategoryEnum.JAVA_MODEL_PACKAGE.toString());
        if (history != null) {
            javaModelPackageText.getChildComponent().setHistory(history);
        }
        javaModelPackageText.setText(modelConfig.getTargetPackage());

        // client
        JavaClientGeneratorConfiguration javaClientConfig = data.getJavaClientConfig();
        if (javaClientConfig.getConfigurationType() == null) {
            javaClientTypeComboBox.setSelectedIndex(0);
        } else {
            javaClientTypeComboBox.setSelectedItem(javaClientConfig.getConfigurationType());
        }
        javaClientProjectText.setText(javaClientConfig.getTargetProject());

        history = paramWrapper.getHistoryMap().get(HistoryCategoryEnum.JAVA_CLIENT_PACKAGE.toString());
        if (history != null) {
            javaClientPackageText.getChildComponent().setHistory(history);
        }
        javaClientPackageText.setText(javaClientConfig.getTargetPackage());

        // sqlmap
        SqlMapGeneratorConfiguration sqlMapConfig = data.getSqlMapConfig();
        sqlMapProjectText.setText(sqlMapConfig.getTargetProject());

        history = paramWrapper.getHistoryMap().get(HistoryCategoryEnum.SQL_MAP_PACKAGE.toString());
        if (history != null) {
            sqlMapPackageText.getChildComponent().setHistory(history);
        }
        sqlMapPackageText.setText(sqlMapConfig.getTargetPackage());
    }

    public void getData(GeneratorParamWrapper data) {
        // context
        data.setBeginningDelimiter(beginningDelimiterText.getText());
        data.setEndingDelimiter(endingDelimiterText.getText());

        // model property
        data.setTrimStrings(trimStringsCheckBox.isSelected());
        data.setDatabaseRemark(databaseRemarkCheckBox.isSelected());

        // plugins
        data.getSelectedPlugins().clear();
        DefaultParameters defaultParameters = settingsHandler.getDefaultParameters();
        if (mapperAnnotationSupportCheckBox.isSelected()) {
            data.getSelectedPlugins().put(MapperAnnotationPlugin.class.getName()
                    , new PluginConfigWrapper(defaultParameters.getMapperAnnotationConfig()));
        }
        if (lombokSupportCheckBox.isSelected()) {
            data.getSelectedPlugins().put(LombokPlugin.class.getName(), null);
        }
        if (selectByPrimaryKeyWithLockCheckBox.isSelected() || selectByExampleWithLockCheckBox.isSelected()) {
            SelectWithLockConfig selectWithLockConfig = defaultParameters.getSelectWithLockConfig();
            selectWithLockConfig.byPrimaryKeyWithLockEnabled = selectByPrimaryKeyWithLockCheckBox.isSelected();
            selectWithLockConfig.byExampleWithLockEnabled = selectByExampleWithLockCheckBox.isSelected();

            data.getSelectedPlugins().put(SelectWithLockPlugin.class.getName(), new PluginConfigWrapper(selectWithLockConfig));
        }

        // default table config
        TableConfigurationWrapper defaultTableConfig = data.getDefaultTableConfigWrapper();
        defaultTableConfig.setInsertStatementEnabled(insertCheckBox.isSelected());
        defaultTableConfig.setUpdateByPrimaryKeyStatementEnabled(updateByPrimaryKeyCheckBox.isSelected());
        defaultTableConfig.setDeleteByPrimaryKeyStatementEnabled(deleteByPrimaryKeyCheckBox.isSelected());
        defaultTableConfig.setSelectByPrimaryKeyStatementEnabled(selectByPrimaryKeyCheckBox.isSelected());

        defaultTableConfig.setSelectByExampleStatementEnabled(selectByExampleCheckbox.isSelected());
        defaultTableConfig.setCountByExampleStatementEnabled(countByExampleCheckbox.isSelected());
        defaultTableConfig.setUpdateByExampleStatementEnabled(updateByExampleCheckbox.isSelected());
        defaultTableConfig.setDeleteByExampleStatementEnabled(deleteByExampleCheckbox.isSelected());

        defaultTableConfig.getDomainObjectRenamingRule().setSearchString(domainSearchText.getText());
        defaultTableConfig.getDomainObjectRenamingRule().setReplaceString(domainReplaceText.getText());

        // generated key prototype
        GeneratedKeyWrapper generatedKeyWrapper = defaultTableConfig.getGeneratedKeyWrapper();
        generatedKeyWrapper.setColumn(columnText.getText());
        generatedKeyWrapper.setIdentity(identityCheckBox.isSelected());
        if (statementComboBox.getSelectedItem() != null) {
            generatedKeyWrapper.setStatement(statementComboBox.getSelectedItem().toString());
        }
        if (keyTypeComboBox.getSelectedItem() != null) {
            generatedKeyWrapper.setType(keyTypeComboBox.getSelectedItem().toString());
        }

        // model config
        JavaModelGeneratorConfiguration javaModelConfig = data.getJavaModelConfig();
        javaModelConfig.setTargetProject(javaModelProjectText.getText());
        javaModelConfig.setTargetPackage(javaModelPackageText.getText());

        // client config
        JavaClientGeneratorConfiguration javaClientConfig = data.getJavaClientConfig();
        javaClientConfig.setConfigurationType((String) javaClientTypeComboBox.getSelectedItem());
        javaClientConfig.setTargetProject(javaClientProjectText.getText());
        javaClientConfig.setTargetPackage(javaClientPackageText.getText());

        // sqlmap config
        SqlMapGeneratorConfiguration sqlMapConfig = data.getSqlMapConfig();
        sqlMapConfig.setTargetProject(sqlMapProjectText.getText());
        sqlMapConfig.setTargetPackage(sqlMapPackageText.getText());
    }

    // validate before pressing OK button
    protected ValidationInfo doValidate() {
        ValidationInfo info = checkTargetProjects(javaModelProjectText, javaClientProjectText,
                (sqlMapGeneratorPanel.isVisible() ? sqlMapProjectText : null));

        if (info == null) {
            getData(paramWrapper);
        } else {
            // select tab pane of the component
            if (info.component != null) {
                ViewUtil.focusTab(info.component);
            }
        }

        return info;
    }

    private ValidationInfo checkTargetProjects(TextFieldWithBrowseButton... textFields) {
        ValidationInfo info = null;
        for (TextFieldWithBrowseButton textField : textFields) {
            if (textField == null) {
                continue;
            }

            String path = textField.getText();
            if (StringUtil.isBlank(path)) {
                info = new ValidationInfo(textField.getToolTipText() + " not specified", textField);
            } else if (!new File(path).exists()) {
                info = new ValidationInfo(textField.getToolTipText() + " does not exist", textField);
            }
            if (info != null) {
                break;
            }
        }
        return info;
    }

    private Action stashAction = new AbstractAction("Stash") {
        @Override
        public void actionPerformed(ActionEvent e) {
            getData(paramWrapper);
            settingsHandler.stashGeneratorParamWrapper(paramWrapper);
            close(CLOSE_EXIT_CODE);
        }
    };

    @Override
    protected Action[] createLeftSideActions() {
        return new Action[]{stashAction};
    }

}