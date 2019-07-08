/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.chuntung.plugin.mybatis.builder.model.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ColumnSettingsHandler {
    private MybatisBuilderService service;

    private String[] FIELD_NAMES = {"action", "columnName", "columnType", "javaType", "javaProperty"};
    private String[] EDITABLE_FIELD_NAMES = {"action", "javaType", "javaProperty"};
    private String[] COLUMN_NAMES = {"Action", "Column name", "Column type", "Java type", "Java property"};


    public static ColumnSettingsHandler getInstance(Project project) {
        return new ColumnSettingsHandler(project);
    }

    ColumnSettingsHandler(Project project) {
        service = MybatisBuilderService.getInstance(project);
    }

    public List<ColumnInfo> fetchColumns(String connectionId, String database, String tableName) {
        ConnectionInfo connectionInfo = null;
        try {
            connectionInfo = service.getConnectionInfoWithPassword(connectionId);
            List<ColumnInfo> list = service.fetchColumns(connectionInfo, new TableInfo(database, tableName));
            return list;
        } catch (SQLException e) {
            Messages.showErrorDialog("Failed to fetch columns, error: " + e.getMessage(), "Database Error");
            return null;
        }
    }

    public ObjectTableModel<ColumnInfo> getObjectTableModel(List<ColumnInfo> items) {
        ObjectTableModel<ColumnInfo> tableModel = new ObjectTableModel<ColumnInfo>(items, FIELD_NAMES, COLUMN_NAMES) {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (columnIndex == 0) {
                    return true;
                }

                Object action = this.getValueAt(rowIndex, 0);
                if (action != null && (ColumnActionEnum.OVERRIDE.equals(action))) {
                    return super.isCellEditable(rowIndex, columnIndex);
                } else {
                    return false;
                }
            }
        };

        tableModel.setEditableFieldNames(EDITABLE_FIELD_NAMES);
        return tableModel;
    }


    public TableCellEditor getActionCellEditor() {
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(ColumnActionEnum.values());
        ComboBox comboBox = new ComboBox(comboBoxModel);
        // display label for action editor
        DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                Component label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    ((JLabel) label).setText(((ColumnActionEnum) value).getLabel());
                }
                return label;
            }
        };
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        comboBox.setRenderer(cellRenderer);
        return new DefaultCellEditor(comboBox);
    }

    public TableCellRenderer getActionCellRenderer() {
        // display label for action
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    ((JLabel) label).setText(((ColumnActionEnum) value).getLabel());
                }
                return label;
            }
        };

        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        return cellRenderer;
    }
}