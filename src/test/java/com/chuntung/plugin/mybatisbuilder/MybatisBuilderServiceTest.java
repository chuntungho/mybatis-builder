/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder;

import com.chuntung.plugin.mybatisbuilder.model.ConnectionInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

public class MybatisBuilderServiceTest {
    private MybatisBuilderService service;

    @Before
    public void init() {
        service = new MybatisBuilderService(null);
    }

    public static ConnectionInfo getTestConnectionInfo() {
        // connection info for test
        ConnectionInfo info = new ConnectionInfo();
        info.setDriverClass("org.sqlite.JDBC");
        info.setUrl("jdbc:sqlite:/Users/tonyho/Downloads/ghost.db");
        info.setUserName("");
        info.setPassword("");

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
    public void fetchDatabases() throws SQLException {
        List<?> databases = service.fetchDatabases(getTestConnectionInfo().getId());
        Assert.assertTrue(databases.size() > 0);
    }

    @Test
    public void fetchTables() throws SQLException {
        List<?> tables = service.fetchTables(getTestConnectionInfo().getId(), "blog");
        Assert.assertTrue(tables.size() > 0);
    }

}