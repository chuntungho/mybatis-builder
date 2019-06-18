/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.plugins;

import com.chuntung.plugin.mybatis.builder.generator.annotation.PluginConfig;
import com.chuntung.plugin.mybatis.builder.util.ConfigUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;
import java.util.Properties;

/**
 * Custom Annotation support.
 *
 * @author Tony Ho
 */
public class MapperAnnotationPlugin extends PluginAdapter {
    public static class Config {
        // default constructor for persistence
        public Config(){
        }

        public Config(String customAnnotationType) {
            this.customAnnotationType = customAnnotationType;
        }

        @PluginConfig(displayName = "Custom Annotation Type", configKey = "customAnnotationType")
        public String customAnnotationType = "";
    }

    private Config config;

    public MapperAnnotationPlugin() {
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);

        config = ConfigUtil.loadFromProperties(properties, Config.class);
    }

    @Override
    public boolean validate(List<String> warnings) {
        if (config.customAnnotationType == null || config.customAnnotationType.isEmpty()) {
            warnings.add("Please specify custom annotation type");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String annotationType = config.customAnnotationType;
        String annotation = '@' + annotationType.substring(annotationType.lastIndexOf('.') + 1);
        interfaze.addAnnotation(annotation);
        interfaze.addImportedType(new FullyQualifiedJavaType(annotationType));

        return true;
    }

}

