/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder;

import com.chuntung.plugin.mybatis.builder.database.SimpleDataSourceFactory;
import com.chuntung.plugin.mybatis.builder.model.ColumnInfo;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.intellij.testFramework.IdeaTestCase;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class MybatisBuilderServiceTest extends IdeaTestCase {
    private MybatisBuilderService service;
    private static ConnectionInfo info;

    protected void setUp() throws Exception {
        super.setUp();
        service = new MybatisBuilderService(myProject);
        service.saveConnectionInfo(Arrays.asList(getTestConnectionInfo()));
    }

    public static ConnectionInfo getTestConnectionInfo() {
        if (MybatisBuilderServiceTest.info != null) {
            return MybatisBuilderServiceTest.info;
        }

        // connection info for test
        ConnectionInfo info = new ConnectionInfo();
        info.setId("junit-test-connection");
        info.setDriverClass("org.h2.Driver");
        info.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        info.setDatabase("TEST");
        info.setUserName("");
        info.setPassword("");

        DataSource dataSource = SimpleDataSourceFactory.getInstance().getDataSource(info);
        try {
            dataSource.getConnection().createStatement().execute("create table user(id int, name varchar(30), sex varchar(1));");
        } catch (SQLException e) {
            fail(e.getMessage());
        }

        MybatisBuilderServiceTest.info = info;

        return info;
    }

    @Test
    public void testConnection() {
        try {
            service.testConnection(getTestConnectionInfo());
        } catch (SQLException e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testFetchDatabases() throws SQLException {
        List<?> databases = service.fetchDatabases(getTestConnectionInfo().getId());
        Assert.assertTrue(databases.size() > 0);
    }

    @Test
    public void testFetchTables() throws SQLException {
        List<?> tables = service.fetchTables(getTestConnectionInfo().getId(), "TEST");
        Assert.assertTrue(tables.size() > 0);
    }

    @Test
    public void testFetchColumns() throws SQLException {
        List<ColumnInfo> columns = service.fetchColumns(getTestConnectionInfo(), new TableInfo("TEST", "USER", null));
        Assert.assertTrue(columns.size() > 0);
    }

}