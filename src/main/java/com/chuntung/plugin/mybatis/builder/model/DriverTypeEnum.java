/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A concrete JDBC driver choice. Drivers are grouped by {@link #getFamily() family}
 * (database vendor); a single family may offer several drivers, e.g. the MySQL family
 * offers MySQL 5, MySQL 8 and MariaDB, and the SQL Server family offers the Microsoft
 * driver and jTDS. The connection-settings driver dropdown is scoped to one family.
 *
 * <p>Constant names are persisted in {@code mybatisbuilder.xml}; never rename existing
 * ones (MySQL / PostgreSQL / Custom and the others already shipped) without a migration.
 */
public enum DriverTypeEnum {

    // --- MySQL family ---
    MySQL("MySQL 5", "MySQL",
            "com.mysql.jdbc.Driver",
            "jdbc:mysql://${host}:${port}/${db}",
            3306,
            "/images/MySQL.png",
            Layout.HOST,
            null,
            mysqlDefaults()),
    MySQL8("MySQL 8", "MySQL",
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://${host}:${port}/${db}",
            3306,
            "/images/MySQL.png",
            Layout.HOST,
            "com.mysql:mysql-connector-j:8.3.0",
            mysqlDefaults()),
    MariaDB("MariaDB", "MySQL",
            "org.mariadb.jdbc.Driver",
            "jdbc:mariadb://${host}:${port}/${db}",
            3306,
            "/images/connection.png",
            Layout.HOST,
            "org.mariadb.jdbc:mariadb-java-client:3.3.3",
            Collections.emptyMap()),

    // --- PostgreSQL family ---
    PostgreSQL("PostgreSQL", "PostgreSQL",
            "org.postgresql.Driver",
            "jdbc:postgresql://${host}:${port}/${db}",
            5432,
            "/images/PostgreSQL.png",
            Layout.HOST,
            null,
            postgresqlDefaults()),

    // --- Oracle family ---
    Oracle("Oracle", "Oracle",
            "oracle.jdbc.OracleDriver",
            "jdbc:oracle:thin:@//${host}:${port}/${db}",
            1521,
            "/images/connection.png",
            Layout.HOST,
            "com.oracle.database.jdbc:ojdbc11:23.4.0.24.05",
            Collections.emptyMap()),

    // --- SQL Server family ---
    SQLServer("SQL Server (Microsoft)", "SQL Server",
            "com.microsoft.sqlserver.jdbc.SQLServerDriver",
            "jdbc:sqlserver://${host}:${port};databaseName=${db};encrypt=false",
            1433,
            "/images/connection.png",
            Layout.HOST,
            "com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11",
            Collections.emptyMap()),
    Jtds("SQL Server (jTDS)", "SQL Server",
            "net.sourceforge.jtds.jdbc.Driver",
            "jdbc:jtds:sqlserver://${host}:${port}/${db}",
            1433,
            "/images/connection.png",
            Layout.HOST,
            "net.sourceforge.jtds:jtds:1.3.1",
            Collections.emptyMap()),

    // --- File-based ---
    SQLite("SQLite", "SQLite",
            "org.sqlite.JDBC",
            "jdbc:sqlite:${db}",
            0,
            "/images/connection.png",
            Layout.FILE,
            "org.xerial:sqlite-jdbc:3.45.1.0",
            Collections.emptyMap()),
    H2("H2", "H2",
            "org.h2.Driver",
            "jdbc:h2:${db}",
            0,
            "/images/connection.png",
            Layout.FILE,
            "com.h2database:h2:2.2.224",
            Collections.emptyMap());

    /**
     * Field layout for the connection form.
     * HOST: host / port / user / password / database fields.
     * FILE: file-based url, host / port hidden.
     */
    public enum Layout {
        HOST, FILE
    }

    private final String displayName;
    private final String family;
    private final String driverClass;
    private final String urlPattern;
    private final Integer defaultPort;
    private final String icon;
    private final Layout layout;
    private final String mavenCoordinate;
    private final Map<String, String> defaultProperties;

    DriverTypeEnum(String displayName, String family, String driverClass, String urlPattern,
                   Integer defaultPort, String icon, Layout layout, String mavenCoordinate,
                   Map<String, String> defaultProperties) {
        this.displayName = displayName;
        this.family = family;
        this.driverClass = driverClass;
        this.urlPattern = urlPattern;
        this.defaultPort = defaultPort;
        this.icon = icon;
        this.layout = layout;
        this.mavenCoordinate = mavenCoordinate;
        this.defaultProperties = Collections.unmodifiableMap(defaultProperties);
    }

    /**
     * Human-friendly driver label shown in the dropdown / add menu (e.g. "MySQL 8").
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Database family this driver belongs to; drivers in the same family are
     * interchangeable in the settings dropdown (e.g. MySQL 5 / MySQL 8 / MariaDB).
     */
    public String getFamily() {
        return family;
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

    public Layout getLayout() {
        return layout;
    }

    /**
     * Maven coordinate (groupId:artifactId:version) of the JDBC driver, or {@code null}
     * when the driver is bundled with the plugin.
     */
    public String getMavenCoordinate() {
        return mavenCoordinate;
    }

    /**
     * @return true when the driver ships inside the plugin and needs no download.
     */
    public boolean isBundled() {
        return mavenCoordinate == null;
    }

    /**
     * @return true when the driver can be downloaded from Maven Central on demand.
     */
    public boolean isDownloadable() {
        return mavenCoordinate != null;
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
