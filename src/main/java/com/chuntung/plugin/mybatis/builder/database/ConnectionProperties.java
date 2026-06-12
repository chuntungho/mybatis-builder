/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ConnectionProperties {
    private ConnectionProperties() {
    }

    public static Map<String, String> resolve(ConnectionInfo info) {
        Map<String, String> merged = new LinkedHashMap<>();
        if (info.getDriverType() != null) {
            merged.putAll(info.getDriverType().getDefaultProperties());
        }
        if (info.getProperties() != null) {
            merged.putAll(info.getProperties());
        }
        return merged;
    }
}
