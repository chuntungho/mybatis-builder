/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.database;

import com.chuntung.plugin.mybatisbuilder.util.StringUtil;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class CustomDataSource implements DataSource {
    private static final Map<String, Driver> driverCache = new HashMap<>();
    private String driverLibrary;
    private String driverClass;
    private String url;
    private String user;
    private String password;

    public CustomDataSource(String driverLibrary, String driverClass) {
        this.driverLibrary = driverLibrary;
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
                Driver driver = (Driver) clazz.newInstance();
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
        return getConnection(user, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (StringUtil.stringHasValue(username)) {
            props.put("user", username);
        }
        if (StringUtil.stringHasValue(password)) {
            props.put("password", password);
        }
        Connection connection = getDriver().connect(url, props);
        return connection;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
