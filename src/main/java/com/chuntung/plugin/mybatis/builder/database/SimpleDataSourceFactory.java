/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DriverTypeEnum;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class SimpleDataSourceFactory {
    private static SimpleDataSourceFactory instance = new SimpleDataSourceFactory();

    public static SimpleDataSourceFactory getInstance() {
        return instance;
    }

    public DataSource getDataSource(ConnectionInfo connectionInfo) {
        if (DriverTypeEnum.MySQL.equals(connectionInfo.getDriverType())) {
            MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setServerName(connectionInfo.getHost());
            dataSource.setPort(connectionInfo.getPort());
            dataSource.setUser(connectionInfo.getUserName());
            dataSource.setPassword(connectionInfo.getPassword());
            dataSource.setDatabaseName(connectionInfo.getDatabase());
            dataSource.setUseSSL(false);
            return dataSource;
        } else if (DriverTypeEnum.PostgreSQL.equals(connectionInfo.getDriverType())) {
            PGSimpleDataSource dataSource = new PGSimpleDataSource();
            dataSource.setUser(connectionInfo.getUserName());
            dataSource.setPassword(connectionInfo.getPassword());
            dataSource.setServerName(connectionInfo.getHost());
            dataSource.setPortNumber(connectionInfo.getPort());
            dataSource.setDatabaseName(connectionInfo.getDatabase());

            return dataSource;
        } else {
            CustomDataSource dataSource = new CustomDataSource(connectionInfo.getDriverLibrary(), connectionInfo.getDriverClass());
            dataSource.setUrl(connectionInfo.getUrl());
            dataSource.setUser(connectionInfo.getUserName());
            dataSource.setPassword(connectionInfo.getPassword());
            return dataSource;
        }

    }
}
