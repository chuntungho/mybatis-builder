/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.view;

import com.chuntung.plugin.mybatisbuilder.action.SettingsHandler;
import com.chuntung.plugin.mybatisbuilder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatisbuilder.generator.TableKey;
import com.chuntung.plugin.mybatisbuilder.model.ObjectTableModel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

public class MybatisBuilderParametersDialog extends DialogWrapper {
    private static final FileChooserDescriptor FOLDER_DESCRIPTOR = new FileChooserDescriptor(false, true, false, false, false, false);
    private JPanel mainPanel;
    private JTextField javaModelPackageText;
    private com.intellij.openapi.ui.TextFieldWithBrowseButton javaModelProjectText;
    private JCheckBox trimStringsCheckBox;
    private JPanel sqlMapGeneratorPanel;
    private JTextField sqlMapPackageText;
    private com.intellij.openapi.ui.TextFieldWithBrowseButton sqlMapProjectText;
    private JTextField javaClientPackageText;
    private JComboBox javaClientTypeCombobox;
    private com.intellij.openapi.ui.TextFieldWithBrowseButton javaClientProjectText;
    private JCheckBox enableCountCheckbox;
    private JCheckBox enableUpdateCheckbox;
    private JCheckBox enableDeleteCheckbox;
    private JCheckBox enableSelectCheckbox;
    private JTable selectedTables;
    private JTextField endingDelimiterText;
    private JTextField beginningDelimiterText;
    private JCheckBox springRepositorySupportCheckBox;
    private JTextField columnText;
    private JTextField statementText;
    private JCheckBox allCheckBox;

    private String[] javaClientTypes = {"XMLMAPPER", "ANNOTATEDMAPPER", "MIXEDMAPPER"};

    private String[] fieldNames = new String[]{"tableName", "domainName", "keyColumn"};
    private String[] editableFieldNames = new String[]{"domainName", "keyColumn"};
    private String[] columnNames = new String[]{"Table Name", "Domain Name", "Key Column"};

    private final SettingsHandler settingsHandler;
    private GeneratorParamWrapper paramWrapper;

    public MybatisBuilderParametersDialog(@Nullable Project project, GeneratorParamWrapper paramWrapper) {
        super(project);
        this.settingsHandler = SettingsHandler.getInstance(project);
        this.paramWrapper = paramWrapper;

        initGUI(project);
        setData(paramWrapper);

        init();
    }

    private ActionListener renderAllListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            renderAll();
        }
    };

    private void renderAll() {
        allCheckBox.setSelected(enableSelectCheckbox.isSelected()
                && enableCountCheckbox.isSelected()
                && enableUpdateCheckbox.isSelected()
                && enableDeleteCheckbox.isSelected());
    }

    private void initGUI(Project project) {
        setTitle("Mybatis Builder - Parameters");

        // check box
        allCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox checkBox = (JCheckBox) e.getSource();
                Boolean checked = checkBox.isSelected();
                enableSelectCheckbox.setSelected(checked);
                enableCountCheckbox.setSelected(checked);
                enableUpdateCheckbox.setSelected(checked);
                enableDeleteCheckbox.setSelected(checked);
            }
        });
        enableSelectCheckbox.addActionListener(renderAllListener);
        enableCountCheckbox.addActionListener(renderAllListener);
        enableUpdateCheckbox.addActionListener(renderAllListener);
        enableDeleteCheckbox.addActionListener(renderAllListener);

        // Combo box
        javaClientTypeCombobox.setModel(new DefaultComboBoxModel(javaClientTypes));
        javaClientTypeCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                sqlMapGeneratorPanel.setVisible(!"ANNOTATEDMAPPER".equals(e.getItem()));
            }
        });

        // directory chooser
        javaModelProjectText.addBrowseFolderListener("Choose Target Project", "", null, FOLDER_DESCRIPTOR);
        javaClientProjectText.addBrowseFolderListener("Choose Target Project", "", null, FOLDER_DESCRIPTOR);
        sqlMapProjectText.addBrowseFolderListener("Choose Target Project", "", null, FOLDER_DESCRIPTOR);
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

        springRepositorySupportCheckBox.setSelected(data.getSpringRepositorySupport());

        // default table config
        TableConfiguration defaultTableConfig = data.getDefaultTableConfig();
        enableSelectCheckbox.setSelected(defaultTableConfig.isSelectByExampleStatementEnabled());
        enableCountCheckbox.setSelected(defaultTableConfig.isCountByExampleStatementEnabled());
        enableUpdateCheckbox.setSelected(defaultTableConfig.isUpdateByExampleStatementEnabled());
        enableDeleteCheckbox.setSelected(defaultTableConfig.isDeleteByExampleStatementEnabled());

        renderAll();

        TableKey defaultTabledKey = data.getDefaultTabledKey();
        columnText.setText(defaultTabledKey.getColumn());
        statementText.setText(defaultTabledKey.getStatement());

        // selected tables
        ObjectTableModel tableModel = new ObjectTableModel(data.getTableList(), fieldNames, columnNames);
        tableModel.setEditableFieldNames(editableFieldNames);
        selectedTables.setModel(tableModel);
        selectedTables.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        // model
        JavaModelGeneratorConfiguration modelConfig = data.getJavaModelConfig();
        javaModelProjectText.setText(modelConfig.getTargetProject());
        javaModelPackageText.setText(modelConfig.getTargetPackage());

        // client
        JavaClientGeneratorConfiguration javaClientConfig = data.getJavaClientConfig();
        if (javaClientConfig.getConfigurationType() == null) {
            javaClientTypeCombobox.setSelectedIndex(0);
        } else {
            javaClientTypeCombobox.setSelectedItem(javaClientConfig.getConfigurationType());
        }
        javaClientProjectText.setText(javaClientConfig.getTargetProject());
        javaClientPackageText.setText(javaClientConfig.getTargetPackage());

        //sqlmap
        SqlMapGeneratorConfiguration sqlMapConfig = data.getSqlMapConfig();
        sqlMapProjectText.setText(sqlMapConfig.getTargetProject());
        sqlMapPackageText.setText(sqlMapConfig.getTargetPackage());
    }

    public void getData(GeneratorParamWrapper data) {
        // context
        data.setBeginningDelimiter(beginningDelimiterText.getText());
        data.setEndingDelimiter(endingDelimiterText.getText());

        // model property
        data.setTrimStrings(trimStringsCheckBox.isSelected());

        // plugin
        data.setSpringRepositorySupport(springRepositorySupportCheckBox.isSelected());

        // default table config
        TableConfiguration defaultTableConfig = data.getDefaultTableConfig();

        defaultTableConfig.setSelectByExampleStatementEnabled(enableSelectCheckbox.isSelected());
        defaultTableConfig.setCountByExampleStatementEnabled(enableCountCheckbox.isSelected());
        defaultTableConfig.setUpdateByExampleStatementEnabled(enableUpdateCheckbox.isSelected());
        defaultTableConfig.setDeleteByExampleStatementEnabled(enableDeleteCheckbox.isSelected());

        TableKey defaultTabledKey = data.getDefaultTabledKey();
        defaultTabledKey.setColumn(columnText.getText());
        defaultTabledKey.setStatement(statementText.getText());

        // model config
        JavaModelGeneratorConfiguration javaModelConfig = data.getJavaModelConfig();
        javaModelConfig.setTargetProject(javaModelProjectText.getText());
        javaModelConfig.setTargetPackage(javaModelPackageText.getText());

        // client config
        JavaClientGeneratorConfiguration javaClientConfig = data.getJavaClientConfig();
        javaClientConfig.setConfigurationType((String) javaClientTypeCombobox.getSelectedItem());
        javaClientConfig.setTargetProject(javaClientProjectText.getText());
        javaClientConfig.setTargetPackage(javaClientPackageText.getText());

        // sqlmap config
        SqlMapGeneratorConfiguration sqlMapConfig = data.getSqlMapConfig();
        sqlMapConfig.setTargetProject(sqlMapProjectText.getText());
        sqlMapConfig.setTargetPackage(sqlMapPackageText.getText());
    }

    // validate before pressing OK button
    protected ValidationInfo doValidate() {
        ValidationInfo info = checkTargetProjects(javaModelProjectText, javaClientProjectText, sqlMapProjectText);

        if (info == null) {
            getData(paramWrapper);
        } else {
            // select tab pane of the component
            if (info.component != null) {
                MybatisBuilderSettingsDialog.focusTab(info.component);
            }
        }

        return info;
    }

    private ValidationInfo checkTargetProjects(TextFieldWithBrowseButton... textFields) {
        ValidationInfo info = null;
        for (TextFieldWithBrowseButton textField : textFields) {
            String path = textField.getText();
            if (StringUtils.isBlank(path)) {
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
    protected Action[] createLeftSideActions(){
        return new Action[]{stashAction};
    }

}