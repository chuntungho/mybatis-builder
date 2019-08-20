/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

import com.chuntung.plugin.mybatis.builder.generator.DefaultParameters;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorParamWrapper;

import java.util.*;

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
    private Map<String, List<String>> historyMap = new HashMap<>();

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

    public Map<String, List<String>> getHistoryMap() {
        return historyMap;
    }

    public void setHistoryMap(Map<String, List<String>> historyMap) {
        this.historyMap = historyMap;
    }
}
