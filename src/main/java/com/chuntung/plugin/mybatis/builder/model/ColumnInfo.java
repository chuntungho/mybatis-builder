/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

import com.intellij.util.xmlb.annotations.Transient;

// column override info
public class ColumnInfo {
    private ColumnActionEnum action;

    private String columnName;

    @Transient
    private String columnType;
    @Transient
    private String comment;

    /** The java property. */
    private String javaProperty;

    /** The java type. */
    private String javaType;

    /** The type handler. */
    private String typeHandler;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ColumnActionEnum getAction() {
        return action;
    }

    public void setAction(ColumnActionEnum action) {
        this.action = action;
    }

    public String getJavaProperty() {
        return javaProperty;
    }

    public void setJavaProperty(String javaProperty) {
        this.javaProperty = javaProperty;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getTypeHandler() {
        return typeHandler;
    }

    public void setTypeHandler(String typeHandler) {
        this.typeHandler = typeHandler;
    }
}
