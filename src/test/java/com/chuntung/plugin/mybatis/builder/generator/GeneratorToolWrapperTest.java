/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderServiceTest;
import com.chuntung.plugin.mybatis.builder.database.ConnectionUrlBuilder;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import org.junit.Test;
import org.mybatis.generator.config.JDBCConnectionConfiguration;

import java.util.Arrays;
import java.util.Properties;

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
        param.getJavaClientConfig().setTargetPackage("mybatis.builder.example.mapper");

        param.getJavaModelConfig().setTargetProject("./src/test/java");
        param.getJavaModelConfig().setTargetPackage("mybatis.builder.example.model");

        param.getSqlMapConfig().setTargetProject("./src/test/resources");
        param.getSqlMapConfig().setTargetPackage("sqlmap");

        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName("user");
        tableInfo.setDomainName("gene.User");
        param.setSelectedTables(Arrays.asList(tableInfo));

        GeneratorToolWrapper tool = new GeneratorToolWrapper(param, null);
        try {
            tool.generate();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void runWithConfigurationFile() {
        try {
            // init mem db
            ConnectionInfo connectionInfo = MybatisBuilderServiceTest.getTestConnectionInfo();

            Properties properties = new Properties();
            properties.setProperty("PROJECT_DIR", ".");
            properties.setProperty("CURRENT_DIR", "./src/test/resources");
            GeneratorToolWrapper.runWithConfigurationFile("./src/test/resources/generator-config.xml", properties, null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}