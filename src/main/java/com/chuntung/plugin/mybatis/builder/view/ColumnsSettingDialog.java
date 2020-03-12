/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.action.ColumnsSettingHandler;
import com.chuntung.plugin.mybatis.builder.model.ColumnActionEnum;
import com.chuntung.plugin.mybatis.builder.model.ColumnInfo;
import com.chuntung.plugin.mybatis.builder.model.ObjectTableModel;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColumnsSettingDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTable columnsTable;
    private JScrollPane columnsPanel;
    private String connectionId;
    private TableInfo tableInfo;
    private ColumnsSettingHandler handler;

    public ColumnsSettingDialog(String connectionId, TableInfo tableInfo, Project project) {
        super(project, false);
        this.connectionId = connectionId;
        this.tableInfo = tableInfo;
        handler = ColumnsSettingHandler.getInstance(project);

        this.setTitle("MyBatis Builder - Columns setting");

        TitledBorder border = (TitledBorder) columnsPanel.getBorder();
        border.setTitle("Columns setting for " + tableInfo.getTableName());

        setData(tableInfo);

        init();
    }

    private void setData(TableInfo tableInfo) {
        List<ColumnInfo> columns = handler.fetchColumns(connectionId, tableInfo.getDatabase(), tableInfo.getTableName());
        // convert to map
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        if (tableInfo.getCustomColumns() != null && !tableInfo.getCustomColumns().isEmpty()) {
            for (ColumnInfo customColumn : tableInfo.getCustomColumns()) {
                columnMap.put(customColumn.getColumnName(), customColumn);
            }
        }

        // merge custom columns
        for (ColumnInfo column : columns) {
            ColumnInfo customColumn = columnMap.get(column.getColumnName());
            if (customColumn != null) {
                column.setAction(customColumn.getAction());
                column.setJavaType(customColumn.getJavaType());
                column.setJavaProperty(customColumn.getJavaProperty());
            } else {
                column.setAction(ColumnActionEnum.DEFAULT);
            }
        }

        handler.initTable(columnsTable, columns);
    }

    private void getData(TableInfo tableInfo) {
        List<ColumnInfo> columns = ((ObjectTableModel<ColumnInfo>) columnsTable.getModel()).getItems();
        List<ColumnInfo> customColumns = new ArrayList<>();
        for (ColumnInfo column : columns) {
            if (!ColumnActionEnum.DEFAULT.equals(column.getAction())) {
                customColumns.add(column);
            }
        }
        tableInfo.setCustomColumns(customColumns);
    }

    protected void doOKAction() {
        getData(tableInfo);
        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}
