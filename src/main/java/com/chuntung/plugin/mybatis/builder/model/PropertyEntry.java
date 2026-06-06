/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.model;

/**
 * Editable row in the connection-properties table.
 */
public class PropertyEntry {
    private String name;
    private String value;

    public PropertyEntry() {
    }

    public PropertyEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
