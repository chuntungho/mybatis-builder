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

    private ConnectionInfo getConnectionInfo() {
        return service.listConnections().get(0);
    }

    @Test
    public void testConnection() throws SQLException {
        boolean connected = service.testConnection(getConnectionInfo());
        Assert.assertTrue(connected);
    }

    @Test
    public void listConnections() {
        List<ConnectionInfo> list = service.listConnections();
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void listDatabases() throws SQLException {
        List<?> databases = service.listDatabases(getConnectionInfo().getId());
        Assert.assertTrue(databases.size() > 0);
    }

    @Test
    public void listTables() throws SQLException {
        List<?> tables = service.listTables(getConnectionInfo().getId(), "blog");
        Assert.assertTrue(tables.size() > 0);
    }

}