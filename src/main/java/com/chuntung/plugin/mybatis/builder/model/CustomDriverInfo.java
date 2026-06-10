/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

/**
 * A user-registered JDBC driver. Registered drivers are persisted in the project
 * settings and can be reused across connections; a connection links to one via
 * {@link ConnectionInfo#getCustomDriverId()} and snapshots the driver class, url
 * template and library path so the JDBC / generator layers stay self-contained.
 *
 * @author Chuntung Ho
 */
public class CustomDriverInfo implements Cloneable {
    private String id;
    private String name;
    private String driverClass;
    // URL template, e.g. jdbc:oracle:thin:@//${host}:${port}/${db}
    private String urlPattern;
    // path to the driver jar
    private String driverLibrary;
    private Integer defaultPort;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getDriverLibrary() {
        return driverLibrary;
    }

    public void setDriverLibrary(String driverLibrary) {
        this.driverLibrary = driverLibrary;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(Integer defaultPort) {
        this.defaultPort = defaultPort;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public CustomDriverInfo clone() {
        try {
            return (CustomDriverInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
