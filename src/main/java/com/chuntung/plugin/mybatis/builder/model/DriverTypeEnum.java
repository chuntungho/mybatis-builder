/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public enum DriverTypeEnum {

    MySQL("com.mysql.jdbc.Driver",
            "jdbc:mysql://${host}:${port}/${db}",
            3306,
            "/images/MySQL.png",
            mysqlDefaults()),
    PostgreSQL("org.postgresql.Driver",
            "jdbc:postgresql://${host}:${port}/${db}",
            5432,
            "/images/PostgreSQL.png",
            postgresqlDefaults()),
    //    Oracle("oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@%s:%s:%s", ""),
    Custom("",
            "jdbc:${vendor}://${host}:${port}/${db}",
            1234,
            "/images/connection.png",
            Collections.emptyMap());

    private final String driverClass;
    private final String urlPattern;
    private final Integer defaultPort;
    private final String icon;
    private final Map<String, String> defaultProperties;

    DriverTypeEnum(String driverClass, String urlPattern, Integer defaultPort,
                   String icon, Map<String, String> defaultProperties) {
        this.driverClass = driverClass;
        this.urlPattern = urlPattern;
        this.defaultPort = defaultPort;
        this.icon = icon;
        this.defaultProperties = Collections.unmodifiableMap(defaultProperties);
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    public String getIcon() {
        return icon;
    }

    public Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    private static Map<String, String> mysqlDefaults() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("useSSL", "false");
        m.put("characterEncoding", "utf-8");
        m.put("useInformationSchema", "true");
        m.put("allowPublicKeyRetrieval", "true");
        m.put("connectTimeout", "5000");
        return m;
    }

    private static Map<String, String> postgresqlDefaults() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("loginTimeout", "5");
        return m;
    }
}