/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.intellij.util.xmlb.annotations.Transient;
import org.mybatis.generator.config.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneratorParamWrapper implements Cloneable {
    private DefaultParameters defaultParameters;
    private String driverLibrary;

    private String beginningDelimiter = "`";
    private String endingDelimiter = "`";
    private Boolean trimStrings = true;
    private Boolean databaseRemark = true;

    private JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
    private JavaModelGeneratorConfiguration javaModelConfig = new JavaModelGeneratorConfiguration();
    private JavaClientGeneratorConfiguration javaClientConfig = new JavaClientGeneratorConfiguration();
    private SqlMapGeneratorConfiguration sqlMapConfig = new SqlMapGeneratorConfiguration();

    private TableConfigurationWrapper defaultTableConfigWrapper = new TableConfigurationWrapper();

    // selected plugins
    private Map<String, PluginConfigWrapper> selectedPlugins = new LinkedHashMap<>();

    // selected tables
    private List<TableInfo> selectedTables;


    @Transient
    public DefaultParameters getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultParameters(DefaultParameters defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    @Transient
    public String getDriverLibrary() {
        return driverLibrary;
    }

    public void setDriverLibrary(String driverLibrary) {
        this.driverLibrary = driverLibrary;
    }

    public String getBeginningDelimiter() {
        return beginningDelimiter;
    }

    public void setBeginningDelimiter(String beginningDelimiter) {
        this.beginningDelimiter = beginningDelimiter;
    }

    public String getEndingDelimiter() {
        return endingDelimiter;
    }

    public void setEndingDelimiter(String endingDelimiter) {
        this.endingDelimiter = endingDelimiter;
    }

    public Boolean getTrimStrings() {
        return trimStrings;
    }

    public void setTrimStrings(Boolean trimStrings) {
        this.trimStrings = trimStrings;
    }

    public Boolean getDatabaseRemark() {
        return databaseRemark;
    }

    public void setDatabaseRemark(Boolean databaseRemark) {
        this.databaseRemark = databaseRemark;
    }

    @Transient
    public JDBCConnectionConfiguration getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(JDBCConnectionConfiguration jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public JavaModelGeneratorConfiguration getJavaModelConfig() {
        return javaModelConfig;
    }

    public void setJavaModelConfig(JavaModelGeneratorConfiguration javaModelConfig) {
        this.javaModelConfig = javaModelConfig;
    }

    public JavaClientGeneratorConfiguration getJavaClientConfig() {
        return javaClientConfig;
    }

    public void setJavaClientConfig(JavaClientGeneratorConfiguration javaClientConfig) {
        this.javaClientConfig = javaClientConfig;
    }

    public SqlMapGeneratorConfiguration getSqlMapConfig() {
        return sqlMapConfig;
    }

    public void setSqlMapConfig(SqlMapGeneratorConfiguration sqlMapConfig) {
        this.sqlMapConfig = sqlMapConfig;
    }

    public TableConfigurationWrapper getDefaultTableConfigWrapper() {
        return defaultTableConfigWrapper;
    }

    public void setDefaultTableConfigWrapper(TableConfigurationWrapper defaultTableConfigWrapper) {
        this.defaultTableConfigWrapper = defaultTableConfigWrapper;
    }

    // save config in JSON, due to persistence issue.
    public Map<String, PluginConfigWrapper> getSelectedPlugins() {
        return selectedPlugins;
    }

    public void setSelectedPlugins(Map<String, PluginConfigWrapper> selectedPlugins) {
        this.selectedPlugins = selectedPlugins;
    }

    @Transient
    public List<TableInfo> getSelectedTables() {
        return selectedTables;
    }

    public void setSelectedTables(List<TableInfo> selectedTables) {
        this.selectedTables = selectedTables;
    }

    public GeneratorParamWrapper clone() {
        try {
            return (GeneratorParamWrapper) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
