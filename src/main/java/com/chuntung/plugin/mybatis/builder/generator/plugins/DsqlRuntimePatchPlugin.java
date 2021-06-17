/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * the plugin to patch Dynamic SQL runtime
 */
public class DsqlRuntimePatchPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return "MyBatis3DynamicSql".equals(context.getTargetRuntime());
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        // enable sub package for support type, this will be resolved in MBG 1.4.1
        // see org.mybatis.generator.api.IntrospectedTable.calculateJavaClientAttributes
        /*if (stringHasValue(introspectedTable.getFullyQualifiedTable().getDomainObjectSubPackage())) {
            String supportType = introspectedTable.getMyBatisDynamicSqlSupportType();
            StringBuilder sb = new StringBuilder();
            int idx = supportType.lastIndexOf('.');
            sb.append(supportType.substring(0, idx + 1));
            sb.append(introspectedTable.getFullyQualifiedTable().getDomainObjectSubPackage());
            sb.append('.');
            sb.append(supportType.substring(idx + 1));
            introspectedTable.setMyBatisDynamicSqlSupportType(sb.toString());
        }*/
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                        IntrospectedColumn introspectedColumn,
                                        IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        context.getCommentGenerator().addFieldComment(field, introspectedTable, introspectedColumn);
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze,
                                   IntrospectedTable introspectedTable) {
        // add generated comment to mapper fields
        for (Field field : interfaze.getFields()) {
            context.getCommentGenerator().addFieldComment(field, introspectedTable);
        }

        // add generated comment to mapper methods
        for (Method method : interfaze.getMethods()) {
            context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        }

        return true;
    }
}
