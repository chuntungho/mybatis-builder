/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * Spring Repository Annotation support.
 *
 * @author Tony Ho
 */
public class SpringRepositoryPlugin extends PluginAdapter {
    private final static String ANNOTATION = "@Repository";
    private final static FullyQualifiedJavaType ANNOTATION_TYPE = new FullyQualifiedJavaType("org.springframework.stereotype.Repository");

    public SpringRepositoryPlugin() {
    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        interfaze.addAnnotation(this.ANNOTATION);
        interfaze.addImportedType(this.ANNOTATION_TYPE);
        return true;
    }
}

