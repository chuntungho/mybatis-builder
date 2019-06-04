/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.internal.DefaultCommentGenerator;

import java.util.Properties;
import java.util.Set;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * Custom comment generator, refer to {@link DefaultCommentGenerator}
 *
 * @author Tony Ho
 */
public class CustomCommentGenerator implements CommentGenerator {
    public static final String ADD_DATABASE_REMARK = "addDatabaseRemark";

    private Properties properties = new Properties();
    private boolean addDatabaseRemark = true;

    public CustomCommentGenerator() {
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        addDatabaseRemark = isTrue(properties.getProperty(ADD_DATABASE_REMARK));
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!addDatabaseRemark) {
            return;
        }

        field.addJavaDocLine("/**");

        StringBuilder sb = new StringBuilder();
        sb.append(" * Column: ").append(introspectedColumn.getActualColumnName());
        field.addJavaDocLine(sb.toString());

        if (StringUtils.isNotBlank(introspectedColumn.getRemarks())) {
            sb.setLength(0);
            sb.append(" * Remark: ").append(introspectedColumn.getRemarks().replace('\n', ' '));
            field.addJavaDocLine(sb.toString());
        }

        field.addJavaDocLine(" */");
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {

    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (!addDatabaseRemark) {
            return;
        }

        topLevelClass.addJavaDocLine("/**");

        StringBuilder sb = new StringBuilder();
        sb.append(" * Table: ").append(introspectedTable.getFullyQualifiedTable());
        topLevelClass.addJavaDocLine(sb.toString());

        topLevelClass.addJavaDocLine(" */");
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean b) {

    }

    @Override
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {

    }

    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {

    }

    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {

    }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        method.addJavaDocLine("/**");

        StringBuilder sb = new StringBuilder(" * ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG).append(" generated automatically, do not modify!");
        method.addJavaDocLine(sb.toString());

        method.addJavaDocLine(" */");
    }

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
    }

    @Override
    public void addComment(XmlElement xmlElement) {
        // support for auto merger, special comment required
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        sb.append(": generated automatically, do not modify!");
        sb.append(" -->");

        xmlElement.addElement(new TextElement(sb.toString()));
    }

    @Override
    public void addRootComment(XmlElement xmlElement) {

    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {

    }

    @Override
    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

    }
}
