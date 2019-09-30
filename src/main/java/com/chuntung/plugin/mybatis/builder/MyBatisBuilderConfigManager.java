/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder;

import com.chuntung.plugin.mybatis.builder.model.MyBatisBuilderConfig;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@State(name = MyBatisBuilderConfigManager.STATE_NAME, storages = @Storage(MyBatisBuilderConfigManager.STORAGE_FILE))
public class MyBatisBuilderConfigManager implements PersistentStateComponent<MyBatisBuilderConfig> {
    static final String STATE_NAME = "MybatisBuilder.application.config";
    static final String STORAGE_FILE = "mybatisbuilder.xml";

    private MyBatisBuilderConfig config = new MyBatisBuilderConfig();

    @Nullable
    @Override
    public MyBatisBuilderConfig getState() {
        return config;
    }

    @Override
    public void loadState(@NotNull MyBatisBuilderConfig config) {
        XmlSerializerUtil.copyBean(config, this.config);
    }

    public String getDeviceId() {
        String deviceId = config.getDeviceId();
        if (!StringUtil.stringHasValue(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            config.setDeviceId(deviceId);
        }
        return deviceId;
    }
}