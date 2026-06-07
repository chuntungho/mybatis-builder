/*
 * Copyright (c) 2019-2024 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import java.util.Properties;

/**
 * Mutable bean for Java client generator settings.
 * Replaces MBG's immutable ClientGeneratorConfiguration for use as a persistent/mutable config holder.
 */
public class JavaClientGeneratorConfig {
    private String configurationType;
    private String targetPackage;
    private String targetProject;
    private transient Properties properties = new Properties();

    public String getConfigurationType() {
        return configurationType;
    }

    public void setConfigurationType(String configurationType) {
        this.configurationType = configurationType;
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getTargetProject() {
        return targetProject;
    }

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public Properties getProperties() {
        return properties;
    }
}
