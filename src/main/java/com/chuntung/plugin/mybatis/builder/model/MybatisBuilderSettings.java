/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

import com.chuntung.plugin.mybatis.builder.generator.DefaultParameters;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatis.builder.generator.TableInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The global project settings.
 *
 * @author Tony Ho
 */
public class MybatisBuilderSettings {
    private List<ConnectionInfo> connectionInfoList = new ArrayList<>();
    private DefaultParameters defaultParameters = new DefaultParameters();
    private GeneratorParamWrapper lastGeneratorParamWrapper = new GeneratorParamWrapper();
    private Map<String, TableInfo> tableInfoMap = new LinkedHashMap<>();

    public List<ConnectionInfo> getConnectionInfoList() {
        return connectionInfoList;
    }

    public void setConnectionInfoList(List<ConnectionInfo> connectionInfoList) {
        this.connectionInfoList = connectionInfoList;
    }

    public DefaultParameters getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultParameters(DefaultParameters defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    public GeneratorParamWrapper getLastGeneratorParamWrapper() {
        return lastGeneratorParamWrapper;
    }

    public void setLastGeneratorParamWrapper(GeneratorParamWrapper lastGeneratorParamWrapper) {
        this.lastGeneratorParamWrapper = lastGeneratorParamWrapper;
    }

    public Map<String, TableInfo> getTableInfoMap() {
        return tableInfoMap;
    }

    public void setTableInfoMap(Map<String, TableInfo> tableInfoMap) {
        this.tableInfoMap = tableInfoMap;
    }

}
