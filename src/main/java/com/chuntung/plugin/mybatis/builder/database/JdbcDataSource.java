/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.util.StringUtil;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class JdbcDataSource implements DataSource {
    private static final Map<String, Driver> driverCache = new HashMap<>();

    private final String driverLibrary;
    private final String driverClass;
    private final String url;
    private final Properties properties;

    public JdbcDataSource(String driverLibrary, String driverClass, String url, Properties properties) {
        this.driverLibrary = driverLibrary;
        this.driverClass = driverClass;
        this.url = url;
        this.properties = properties != null ? properties : new Properties();
    }

    public String getUrl() {
        return url;
    }

    private Driver getDriver() throws SQLException {
        ClassLoader parentClassLoader = getClass().getClassLoader();
        String key = driverClass + "@" + driverLibrary;
        if (!driverCache.containsKey(key)) {
            try {
                URL[] urls = {};
                // support built-in driver with empty library
                if (driverLibrary != null && !driverLibrary.isEmpty()) {
                    urls = new URL[]{new File(driverLibrary).toURI().toURL()};
                }
                URLClassLoader classLoader = URLClassLoader.newInstance(urls, parentClassLoader);
                Class<?> clazz = classLoader.loadClass(driverClass);
                Driver driver = (Driver) clazz.getDeclaredConstructor().newInstance();
                DriverManager.registerDriver(driver);
                driverCache.put(key, driver);
            } catch (Exception e) {
                throw new SQLException("Driver initialization failed, error: " + e.getMessage());
            }
        }
        return driverCache.get(key);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDriver().connect(url, properties);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties override = new Properties();
        override.putAll(properties);
        if (StringUtil.stringHasValue(username)) {
            override.setProperty("user", username);
        }
        if (password != null) {
            override.setProperty("password", password);
        }
        return getDriver().connect(url, override);
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
