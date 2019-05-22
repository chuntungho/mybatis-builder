/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * A plugin that support Spring Repository Annotation.
 *
 * @author Tony Ho
 */
public class SpringRepositoryPlugin extends PluginAdapter {
    private FullyQualifiedJavaType annotationRepository = new FullyQualifiedJavaType("org.springframework.stereotype.Repository");
    private String annotation = "@Repository";

    public SpringRepositoryPlugin() {
    }

    public boolean validate(List<String> list) {
        return true;
    }

    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        interfaze.addImportedType(this.annotationRepository);
        interfaze.addAnnotation(this.annotation);
        return true;
    }
}

