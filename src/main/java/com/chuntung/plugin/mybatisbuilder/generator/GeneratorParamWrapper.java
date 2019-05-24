/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import com.intellij.util.xmlb.annotations.Transient;
import org.mybatis.generator.config.*;

import java.util.List;

public class GeneratorParamWrapper implements Cloneable {
    private DefaultParameters defaultParameters;
    private String driverLibrary;

    private String beginningDelimiter = "`";
    private String endingDelimiter = "`";
    private Boolean trimStrings = true;
    private Boolean springRepositorySupport = true;
    // TODO selectWithLockSupport = true;

    private JDBCConnectionConfiguration jdbcConfig = new JDBCConnectionConfiguration();
    private JavaModelGeneratorConfiguration javaModelConfig = new JavaModelGeneratorConfiguration();
    private JavaClientGeneratorConfiguration javaClientConfig = new JavaClientGeneratorConfiguration();
    private SqlMapGeneratorConfiguration sqlMapConfig = new SqlMapGeneratorConfiguration();

    private TableConfiguration defaultTableConfig = new TableConfiguration(new Context(null));
    private ColumnRenamingRule defaultColumnConfig = new ColumnRenamingRule();
    private TableKey defaultTabledKey = new TableKey();

    private List<TableInfo> tableList;

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

    public TableConfiguration getDefaultTableConfig() {
        return defaultTableConfig;
    }

    public void setDefaultTableConfig(TableConfiguration defaultTableConfig) {
        this.defaultTableConfig = defaultTableConfig;
    }

    public ColumnRenamingRule getDefaultColumnConfig() {
        return defaultColumnConfig;
    }

    public void setDefaultColumnConfig(ColumnRenamingRule defaultColumnConfig) {
        this.defaultColumnConfig = defaultColumnConfig;
    }

    public TableKey getDefaultTabledKey() {
        return defaultTabledKey;
    }

    public void setDefaultTabledKey(TableKey defaultTabledKey) {
        this.defaultTabledKey = defaultTabledKey;
    }

    @Transient
    public List<TableInfo> getTableList() {
        return tableList;
    }

    public void setTableList(List<TableInfo> tableList) {
        this.tableList = tableList;
    }

    public Boolean getTrimStrings() {
        return trimStrings;
    }

    public void setTrimStrings(Boolean trimStrings) {
        this.trimStrings = trimStrings;
    }

    public Boolean getSpringRepositorySupport() {
        return springRepositorySupport;
    }

    public void setSpringRepositorySupport(Boolean springRepositorySupport) {
        this.springRepositorySupport = springRepositorySupport;
    }

    public GeneratorParamWrapper clone() {
        try {
            return (GeneratorParamWrapper) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
