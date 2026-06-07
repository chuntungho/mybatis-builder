/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.internal.DefaultCommentGenerator;

import java.sql.Types;
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
    public static final String GENERATED_COMMENT = "generatedComment";

    private Properties properties = new Properties();
    private boolean addDatabaseRemark = true;
    private String generatedComment;

    public CustomCommentGenerator() {
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        addDatabaseRemark = isTrue(properties.getProperty(ADD_DATABASE_REMARK));
        generatedComment = properties.getProperty(GENERATED_COMMENT);
    }

    // addFieldComment is not in the MBG 2.0.0 CommentGenerator interface but kept for compatibility
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        if (!addDatabaseRemark) {
            return;
        }

        field.addJavaDocLine("/**");

        StringBuilder sb = new StringBuilder();
        sb.append(" * Column: ").append(introspectedColumn.getActualColumnName());
        field.addJavaDocLine(sb.toString());

        sb.setLength(0);
        sb.append(" * Type: ").append(introspectedColumn.getActualTypeName());
        // append length for tinyint, char, varchar
        if (Types.CHAR == introspectedColumn.getJdbcType() ||
                Types.VARCHAR == introspectedColumn.getJdbcType() ||
                Types.TINYINT == introspectedColumn.getJdbcType()) {
            sb.append("(").append(introspectedColumn.getLength()).append(")");
        }
        field.addJavaDocLine(sb.toString());

        String defaultVal = introspectedColumn.getDefaultValue().orElse(null);
        if (StringUtil.stringHasValue(defaultVal)) {
            sb.setLength(0);
            sb.append(" * Default value: ").append(defaultVal);
            field.addJavaDocLine(sb.toString());
        }

        String remarks = introspectedColumn.getRemarks().orElse(null);
        if (StringUtil.stringHasValue(remarks)) {
            sb.setLength(0);
            sb.append(" * Remark: ").append(remarks.replace('\n', ' '));
            field.addJavaDocLine(sb.toString());
        }

        field.addJavaDocLine(" */");
    }

    // addFieldComment(Field, IntrospectedTable) is not in MBG 2.0.0 interface
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        addJavaDocComment(field);
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

    // addClassComment is not in MBG 2.0.0 interface
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
    }

    // addClassComment(bool) is not in MBG 2.0.0 interface
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean b) {
    }

    // addEnumComment is not in MBG 2.0.0 interface
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
    }

    // addGetterComment is not in MBG 2.0.0 interface
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
    }

    // addSetterComment is not in MBG 2.0.0 interface
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
    }

    // addGeneralMethodComment is not in MBG 2.0.0 interface
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        addJavaDocComment(method);
    }

    private void addJavaDocComment(JavaElement el) {
        el.addJavaDocLine("/**");
        StringBuilder sb = new StringBuilder(" * ");
        sb.append(MergeConstants.NEW_ELEMENT_TAG);
        if (StringUtil.stringHasValue(generatedComment)) {
            sb.append(' ').append(generatedComment);
        }
        el.addJavaDocLine(sb.toString());
        el.addJavaDocLine(" */");
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
        if (StringUtil.stringHasValue(generatedComment)) {
            sb.append(": ").append(generatedComment);
        }
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
