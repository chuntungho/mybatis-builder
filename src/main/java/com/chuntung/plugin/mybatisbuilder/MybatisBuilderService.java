/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder;

import com.chuntung.plugin.mybatisbuilder.database.SimpleDataSourceFactory;
import com.chuntung.plugin.mybatisbuilder.generator.DefaultParameters;
import com.chuntung.plugin.mybatisbuilder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatisbuilder.generator.TableInfo;
import com.chuntung.plugin.mybatisbuilder.model.ConnectionInfo;
import com.chuntung.plugin.mybatisbuilder.model.DatabaseItem;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The unified plugin service.
 *
 * @author Tony Ho
 */
public class MybatisBuilderService implements ProjectComponent {
    private static final Logger logger = LoggerFactory.getLogger(MybatisBuilderService.class);

    private MybatisBuilderSettingsManager manager;

    public static MybatisBuilderService getInstance(Project project) {
        return project.getComponent(MybatisBuilderService.class);
    }

    public MybatisBuilderService(Project project) {
        if (project != null) {
            this.manager = project.getComponent(MybatisBuilderSettingsManager.class);
        }
    }

    public void saveConnectionInfo(List<ConnectionInfo> connectionInfoList) {
        manager.saveConnectionInfo(connectionInfoList);
    }

    public void testConnection(ConnectionInfo connectionInfo) throws SQLException {
        DataSource dataSource = SimpleDataSourceFactory.getInstance().getDataSource(connectionInfo);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } finally {
            close(connection);
        }
    }

    public List<ConnectionInfo> loadConnectionInfoList() {
        return manager.getSettings().getConnectionInfoList();
    }

    // return cloned connections with password
    public List<ConnectionInfo> loadConnectionInfoListWithPassword() {
        List<ConnectionInfo> list = new ArrayList<>();
        for (ConnectionInfo connection : this.loadConnectionInfoList()) {
            ConnectionInfo dto = connection.clone();
            populatePassword(dto);
            list.add(dto);
        }

        return list;
    }

    private void populatePassword(ConnectionInfo connectionInfo) {
        String password = manager.getConnectionPassword(connectionInfo);
        if (password != null) {
            connectionInfo.setPassword(password);
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Failed to close connection", e);
            }
        }
    }

    // return cloned connection info with password
    public ConnectionInfo getConnectionInfoWithPassword(String connectionId) throws SQLException {
        ConnectionInfo result = null;
        List<ConnectionInfo> connectionInfos = loadConnectionInfoList();
        for (ConnectionInfo connectionInfo : connectionInfos) {
            if (connectionInfo.getId().equals(connectionId)) {
                result = connectionInfo.clone();
            }
        }

        if (result == null) {
            throw new SQLException("Connection name not found, please add it first");
        }

        populatePassword(result);

        return result;
    }

    public List<DatabaseItem> fetchDatabases(String connectionId) throws SQLException {
        List<DatabaseItem> list = new ArrayList<>();
        ConnectionInfo connectionInfo = getConnectionInfoWithPassword(connectionId);
        DataSource dataSource = SimpleDataSourceFactory.getInstance().getDataSource(connectionInfo);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData meta = connection.getMetaData();

            ResultSet catalogs = meta.getCatalogs();
            while (catalogs.next()) {
                String catalog = catalogs.getString(1);
                if (StringUtils.isNotEmpty(catalog)) {
                    list.add(DatabaseItem.of(DatabaseItem.ItemTypeEnum.DATABASE, catalog));
                }
            }

            // oracle use schema for catalog
            if (list.size() == 0 && isOracle(connection)) {
                catalogs = meta.getSchemas();
                while (catalogs.next()) {
                    String catalog = catalogs.getString(1);
                    if (StringUtils.isNotEmpty(catalog)) {
                        list.add(DatabaseItem.of(DatabaseItem.ItemTypeEnum.DATABASE, catalog));
                    }
                }
            }

            // NOTE: hard-code for SQLite which have no catalog
            if (list.size() == 0 && "SQLiteConnection".equals(connection.getClass().getSimpleName())) {
                list.add(DatabaseItem.of(DatabaseItem.ItemTypeEnum.DATABASE, "dummy"));
            }
        } finally {
            close(connection);
        }
        return list;
    }

    private boolean isOracle(Connection connection) {
        return connection.getClass().getName().startsWith("oracle");
    }

    public List<DatabaseItem> fetchTables(String connectionId, String database) throws SQLException {
        List<DatabaseItem> list = new ArrayList<>();
        ConnectionInfo connectionInfo = getConnectionInfoWithPassword(connectionId);
        DataSource dataSource = SimpleDataSourceFactory.getInstance().getDataSource(connectionInfo);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            String catalog = database, schema = null;

            // oracle should specify schema
            if(isOracle(connection)) {
                schema = database;
            }

            ResultSet tables = meta.getTables(catalog, schema, null, new String[]{"TABLE"});
            while (tables.next()) {
                list.add(DatabaseItem.of(DatabaseItem.ItemTypeEnum.TABLE, tables.getString("TABLE_NAME")));
            }
        } finally {
            close(connection);
        }
        return list;
    }

    public void stashGeneratorParamWrapper(GeneratorParamWrapper paramWrapper) {
        manager.getSettings().setLastGeneratorParamWrapper(paramWrapper);
        manager.saveTableInfo(paramWrapper.getSelectedTables());
    }

    public GeneratorParamWrapper getLastGeneratorParamWrapper() {
        return manager.getSettings().getLastGeneratorParamWrapper();
    }

    public TableInfo getLastTableInfo(TableInfo param) {
        return manager.getTableInfo(param);
    }

    public DefaultParameters getDefaultParameters() {
        DefaultParameters instance = new DefaultParameters();
        DefaultParameters saved = manager.getSettings().getDefaultParameters();
        XmlSerializerUtil.copyBean(saved, instance);
        return instance;
    }

    public void saveDefaultParameters(DefaultParameters defaultParameters) {
        manager.getSettings().setDefaultParameters(defaultParameters);
    }
}
