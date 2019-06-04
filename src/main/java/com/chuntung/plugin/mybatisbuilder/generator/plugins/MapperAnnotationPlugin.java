/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator.plugins;

import com.chuntung.plugin.mybatisbuilder.generator.annotation.PluginConfig;
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
    public static final String CUSTOM_ANNOTATION_TYPE = "CUSTOM_ANNOTATION_TYPE";

    public static class Config {
        public Config(String customAnnotationType) {
            this.customAnnotationType = customAnnotationType;
        }

        @PluginConfig(displayName = "Custom Annotation Type", configKey = CUSTOM_ANNOTATION_TYPE)
        public String customAnnotationType = "";
    }

    private String annotationType;

    public MapperAnnotationPlugin() {
    }

    @Override
    public void setProperties(Properties properties) {
        annotationType = properties.getProperty(CUSTOM_ANNOTATION_TYPE);
        super.setProperties(properties);
    }

    @Override
    public boolean validate(List<String> warnings) {
        if (annotationType == null || annotationType.isEmpty()) {
            warnings.add("Please specify custom annotation type");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String annotation = '@' + annotationType.substring(annotationType.lastIndexOf('.') + 1);
        interfaze.addAnnotation(annotation);
        interfaze.addImportedType(new FullyQualifiedJavaType(annotationType));

        return true;
    }

}

