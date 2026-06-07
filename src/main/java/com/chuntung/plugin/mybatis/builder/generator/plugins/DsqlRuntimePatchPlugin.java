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

/**
 * the plugin to patch Dynamic SQL runtime
 */
public class DsqlRuntimePatchPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return context.getTargetRuntime().map("MyBatis3DynamicSql"::equals).orElse(false);
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass,
                                        IntrospectedColumn introspectedColumn,
                                        IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // addFieldComment removed from CommentGenerator interface in MBG 2.0.0
        if (commentGenerator instanceof com.chuntung.plugin.mybatis.builder.generator.CustomCommentGenerator) {
            ((com.chuntung.plugin.mybatis.builder.generator.CustomCommentGenerator) commentGenerator)
                    .addFieldComment(field, introspectedTable, introspectedColumn);
        }
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze,
                                   IntrospectedTable introspectedTable) {
        if (commentGenerator instanceof com.chuntung.plugin.mybatis.builder.generator.CustomCommentGenerator) {
            com.chuntung.plugin.mybatis.builder.generator.CustomCommentGenerator cg =
                    (com.chuntung.plugin.mybatis.builder.generator.CustomCommentGenerator) commentGenerator;
            for (Field field : interfaze.getFields()) {
                cg.addFieldComment(field, introspectedTable);
            }
            for (Method method : interfaze.getMethods()) {
                cg.addGeneralMethodComment(method, introspectedTable);
            }
        }
        return true;
    }
}
