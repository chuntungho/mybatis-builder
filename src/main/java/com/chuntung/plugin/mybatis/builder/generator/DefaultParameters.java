/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.generator.plugins.MapperAnnotationPlugin;
import com.chuntung.plugin.mybatis.builder.generator.plugins.RenamePlugin;
import com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock.SelectWithLockConfig;
import org.mybatis.generator.config.ModelType;

public class DefaultParameters {

    private ModelType defaultModelType = ModelType.FLAT;
    private String javaFileEncoding = "UTF-8";
    private Boolean forceBigDecimals = true;
    private Boolean useJSR310Types = false;
    private String generatedComment = "generated automatically, do not modify!";
    private Integer historySize = 10;

    // plugin configs

    private MapperAnnotationPlugin.Config mapperAnnotationConfig
            = new MapperAnnotationPlugin.Config("org.springframework.stereotype.Repository");

    private SelectWithLockConfig selectWithLockConfig = new SelectWithLockConfig();

    private RenamePlugin.Config renameConfig = new RenamePlugin.Config();

    public ModelType getDefaultModelType() {
        return defaultModelType;
    }

    public void setDefaultModelType(ModelType defaultModelType) {
        this.defaultModelType = defaultModelType;
    }

    public String getJavaFileEncoding() {
        return javaFileEncoding;
    }

    public void setJavaFileEncoding(String javaFileEncoding) {
        this.javaFileEncoding = javaFileEncoding;
    }

    public String getGeneratedComment() {
        return generatedComment;
    }

    public void setGeneratedComment(String generatedComment) {
        this.generatedComment = generatedComment;
    }

    public Boolean getForceBigDecimals() {
        return forceBigDecimals;
    }

    public void setForceBigDecimals(Boolean forceBigDecimals) {
        this.forceBigDecimals = forceBigDecimals;
    }

    public Boolean getUseJSR310Types() {
        return useJSR310Types;
    }

    public void setUseJSR310Types(Boolean useJSR310Types) {
        this.useJSR310Types = useJSR310Types;
    }

    public Integer getHistorySize() {
        return historySize;
    }

    public void setHistorySize(Integer historySize) {
        this.historySize = historySize;
    }

    public MapperAnnotationPlugin.Config getMapperAnnotationConfig() {
        return mapperAnnotationConfig;
    }

    public void setMapperAnnotationConfig(MapperAnnotationPlugin.Config mapperAnnotationConfig) {
        this.mapperAnnotationConfig = mapperAnnotationConfig;
    }

    public SelectWithLockConfig getSelectWithLockConfig() {
        return selectWithLockConfig;
    }

    public void setSelectWithLockConfig(SelectWithLockConfig selectWithLockConfig) {
        this.selectWithLockConfig = selectWithLockConfig;
    }

    public RenamePlugin.Config getRenameConfig() {
        return renameConfig;
    }

    public void setRenameConfig(RenamePlugin.Config renameConfig) {
        this.renameConfig = renameConfig;
    }
}
