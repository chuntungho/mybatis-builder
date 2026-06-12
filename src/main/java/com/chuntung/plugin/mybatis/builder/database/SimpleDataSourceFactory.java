/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

public class SimpleDataSourceFactory {
    private static final SimpleDataSourceFactory instance = new SimpleDataSourceFactory();

    public static SimpleDataSourceFactory getInstance() {
        return instance;
    }

    public DataSource getDataSource(ConnectionInfo connectionInfo) {
        String driverClass = StringUtil.stringHasValue(connectionInfo.getDriverClass())
                ? connectionInfo.getDriverClass()
                : (connectionInfo.getDriverType() != null ? connectionInfo.getDriverType().getDriverClass() : "");
        String url = new ConnectionUrlBuilder(connectionInfo).getConnectionUrl();

        Properties props = new Properties();
        for (Map.Entry<String, String> entry : ConnectionProperties.resolve(connectionInfo).entrySet()) {
            props.setProperty(entry.getKey(), entry.getValue());
        }
        if (StringUtil.stringHasValue(connectionInfo.getUserName())) {
            props.setProperty("user", connectionInfo.getUserName());
        }
        if (connectionInfo.getPassword() != null) {
            props.setProperty("password", connectionInfo.getPassword());
        }
        props.setProperty("remarks", "true");

        String driverLibrary = DriverDownloader.getInstance().resolveDriverLibrary(connectionInfo);
        return new JdbcDataSource(driverLibrary, driverClass, url, props);
    }
}
