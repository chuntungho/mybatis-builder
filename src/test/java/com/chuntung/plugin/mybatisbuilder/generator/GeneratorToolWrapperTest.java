/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import com.chuntung.plugin.mybatisbuilder.MybatisBuilderServiceTest;
import com.chuntung.plugin.mybatisbuilder.database.ConnectionUrlBuilder;
import com.chuntung.plugin.mybatisbuilder.model.ConnectionInfo;
import org.junit.Test;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

import java.util.Arrays;

import static org.junit.Assert.fail;

public class GeneratorToolWrapperTest {

    @Test
    public void generate() {
        GeneratorParamWrapper param = new GeneratorParamWrapper();
        param.setDefaultParameters(new DefaultParameters());

        JDBCConnectionConfiguration jdbcConfig = param.getJdbcConfig();
        ConnectionInfo connectionInfo = MybatisBuilderServiceTest.getTestConnectionInfo();
        String connectionUrl = new ConnectionUrlBuilder(connectionInfo).getConnectionUrl();
        jdbcConfig.setConnectionURL(connectionUrl);
        jdbcConfig.setDriverClass(connectionInfo.getDriverClass());
        jdbcConfig.setUserId(connectionInfo.getUserName());
        jdbcConfig.setPassword(connectionInfo.getPassword());

        param.getJavaClientConfig().setConfigurationType("XMLMAPPER");
        param.getJavaClientConfig().setTargetProject("./src/test/java");
        param.getJavaClientConfig().setTargetPackage("mybatisbuilder.example.mapper");

        param.getJavaModelConfig().setTargetProject("./src/test/java");
        param.getJavaModelConfig().setTargetPackage("mybatisbuilder.example.model");

        param.getSqlMapConfig().setTargetProject("./src/test/resources");
        param.getSqlMapConfig().setTargetPackage("sqlmap");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("actions");
        tableInfo.setDomainName("Action");
        param.setSelectedTables(Arrays.asList(tableInfo));

        GeneratorToolWrapper tool = new GeneratorToolWrapper(param);
        try {
            tool.generate();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void runWithConfigurationFile() {
        try {
            GeneratorToolWrapper.runWithConfigurationFile("./src/test/resources/generator-config.xml");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}