/*
 * Copyright (c) 2019-2024 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import java.util.Properties;

/**
 * Mutable bean for JDBC connection settings.
 * Replaces MBG's immutable JDBCConnectionConfiguration for use as a mutable config holder.
 */
public class JdbcConnectionConfig {
    private String driverClass;
    private String connectionURL;
    private String userId;
    private String password;
    private Properties properties = new Properties();

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getConnectionURL() {
        return connectionURL;
    }

    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public Properties getProperties() {
        return properties;
    }
}
