/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
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
    // connection repo
    private List<ConnectionInfo> connectionInfoList = new ArrayList<>();
    // default config repo
    private DefaultParameters defaultParameters = new DefaultParameters();
    // last param repo
    private GeneratorParamWrapper lastGeneratorParamWrapper = new GeneratorParamWrapper();
    // stash history repo
    private Map<String, GeneratorParamWrapper> stashMap = new LinkedHashMap<>(16, 0.75f, true);
    // table info repo
    private Map<String, TableInfo> tableInfoMap = new LinkedHashMap<>();
    // package dropdown history repo
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

    public Map<String, GeneratorParamWrapper> getStashMap() {
        return stashMap;
    }

    public void setStashMap(Map<String, GeneratorParamWrapper> stashMap) {
        this.stashMap = stashMap;
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
