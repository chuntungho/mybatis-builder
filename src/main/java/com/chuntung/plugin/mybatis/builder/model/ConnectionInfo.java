/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

import com.intellij.util.xmlb.annotations.Transient;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Connection Info Model
 *
 * @author Tony Ho
 */
public class ConnectionInfo implements Cloneable {
    private String id;
    private String name;
    private String description;
    private Boolean active = Boolean.TRUE;
    private DriverTypeEnum driverType;
    // id of the registered driver when this connection uses a user-registered driver
    // (driverType is null in that case)
    private String customDriverId;
    private String driverLibrary;
    private String driverClass;
    // literal url override; when blank the url is built from urlPattern + host/port/db
    private String url;
    // url template, populated for registered-driver connections (driverType == null)
    private String urlPattern;
    private String host;
    private Integer port;
    private String database;
    private String userName;
    private String password;
    private Map<String, String> properties = new LinkedHashMap<>();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public DriverTypeEnum getDriverType() {
        return driverType;
    }

    public void setDriverType(DriverTypeEnum driverType) {
        this.driverType = driverType;
    }

    public String getCustomDriverId() {
        return customDriverId;
    }

    public void setCustomDriverId(String customDriverId) {
        this.customDriverId = customDriverId;
    }

    public String getDriverLibrary() {
        return driverLibrary;
    }

    public void setDriverLibrary(String driverLibrary) {
        this.driverLibrary = driverLibrary;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Transient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties != null ? properties : new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConnectionInfo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", active=").append(active);
        sb.append(", driverType=").append(driverType);
        sb.append(", customDriverId='").append(customDriverId).append('\'');
        sb.append(", driverLibrary='").append(driverLibrary).append('\'');
        sb.append(", driverClass='").append(driverClass).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", urlPattern='").append(urlPattern).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", database='").append(database).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

    public ConnectionInfo clone() {
        try {
            ConnectionInfo copy = (ConnectionInfo) super.clone();
            copy.properties = new LinkedHashMap<>(this.properties);
            return copy;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
