/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.model;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Simple Object table model.
 *
 * @author Tony Ho
 */
public class ObjectTableModel<T> extends AbstractTableModel {
    private List<T> items;
    private String[] fieldNames;
    private String[] columnNames;
    private String[] editableFieldNames;

    public ObjectTableModel(List<T> items, String[] fieldNames, String[] columnNames) {
        this.items = items;
        this.fieldNames = fieldNames;
        this.columnNames = columnNames;
    }

    public List<T> getItems(){
        return items;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return fieldNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object item = items.get(rowIndex);
        String fieldName = fieldNames[columnIndex];

        return getProperty(item, fieldName);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String fieldName = fieldNames[columnIndex];
        if (editableFieldNames != null) {
            for (String editableField : editableFieldNames) {
                if (editableField.equals(fieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getColumnName(int i) {
        if (columnNames == null) {
            return fieldNames[i];
        } else {
            return columnNames[i];
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Object item = items.get(rowIndex);
        String fieldName = fieldNames[columnIndex];
        setProperty(item, fieldName, aValue);
    }

    private void setProperty(Object bean, String fieldName, Object val) {
        try {
            String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method method = bean.getClass().getDeclaredMethod(methodName, val.getClass());
            method.invoke(bean, val);
        } catch (Exception e) {
            // NULL
        }
    }

    private Object getProperty(Object bean, String fieldName) {
        try {
            String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            Method method = bean.getClass().getDeclaredMethod(methodName);
            return method.invoke(bean);
        } catch (Exception e) {
            return "--";
        }
    }

    public void setEditableFieldNames(String[] editableFieldNames) {
        this.editableFieldNames = editableFieldNames;
    }
}
