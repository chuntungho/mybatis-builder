/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeneratorParamWrapper implements Cloneable {
    public static final String MY_BATIS_3_DYNAMIC_SQL = "MyBatis3DynamicSql";

    private String stashName;
    private String connectionId;

    // reload default parameters every time
    private DefaultParameters defaultParameters;

    // just for ui usage
    private Map<String, ? extends List<String>> historyMap;

    private String targetRuntime;

    private String beginningDelimiter = "`";
    private String endingDelimiter = "`";
    private Boolean trimStrings = true;
    private Boolean databaseRemark = true;

    // derived from connection
    private String driverLibrary;
    @Transient
    private JdbcConnectionConfig jdbcConfig = new JdbcConnectionConfig();

    private JavaModelGeneratorConfig javaModelConfig = new JavaModelGeneratorConfig();
    private JavaClientGeneratorConfig javaClientConfig = new JavaClientGeneratorConfig();
    private SqlMapGeneratorConfig sqlMapConfig = new SqlMapGeneratorConfig();

    private TableConfigurationWrapper defaultTableConfigWrapper = new TableConfigurationWrapper();

    // selected plugins
    private Map<String, PluginConfigWrapper> selectedPlugins = new LinkedHashMap<>();

    // selected tables
    private List<TableInfo> selectedTables;

    public String getTargetRuntime() {
        return targetRuntime;
    }

    public void setTargetRuntime(String targetRuntime) {
        this.targetRuntime = targetRuntime;
    }

    @Transient
    public DefaultParameters getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultParameters(DefaultParameters defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    @Transient
    public Map<String, ? extends List<String>> getHistoryMap() {
        return historyMap;
    }

    public void setHistoryMap(Map<String, ? extends List<String>> historyMap) {
        this.historyMap = historyMap;
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

    public JdbcConnectionConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(JdbcConnectionConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public JavaModelGeneratorConfig getJavaModelConfig() {
        return javaModelConfig;
    }

    public void setJavaModelConfig(JavaModelGeneratorConfig javaModelConfig) {
        this.javaModelConfig = javaModelConfig;
    }

    public JavaClientGeneratorConfig getJavaClientConfig() {
        return javaClientConfig;
    }

    public void setJavaClientConfig(JavaClientGeneratorConfig javaClientConfig) {
        this.javaClientConfig = javaClientConfig;
    }

    public SqlMapGeneratorConfig getSqlMapConfig() {
        return sqlMapConfig;
    }

    public void setSqlMapConfig(SqlMapGeneratorConfig sqlMapConfig) {
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
