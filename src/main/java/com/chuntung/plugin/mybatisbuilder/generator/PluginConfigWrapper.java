/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import com.chuntung.plugin.mybatisbuilder.util.JsonUtil;

public class PluginConfigWrapper<T> {
    private String type;
    private String json;
    private T configBean;

    public PluginConfigWrapper() {
    }

    public PluginConfigWrapper(T configBean) {
        this.type = configBean.getClass().getName();
        this.json = JsonUtil.toJson(configBean);
        this.configBean = configBean;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
        this.configBean = null;
    }

    public T getPluginConfig() {
        if (configBean != null) {
            return configBean;
        }

        Class<T> clazz = null;
        try {
            clazz = (Class<T>) Class.forName(type);
            configBean = JsonUtil.fromJson(json, clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return configBean;
    }
}
