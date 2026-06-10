/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.MyBatisGenerator;
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

    // The merger (MBG's JavaFileMergerJavaParserImpl) recognises generated members by the
    // @Generated annotation whose value equals MyBatisGenerator's class name; match MBG default.
    private static final FullyQualifiedJavaType GENERATED_ANNOTATION_TYPE =
            new FullyQualifiedJavaType("jakarta.annotation.Generated");
    private static final String GENERATED_ANNOTATION_VALUE = MyBatisGenerator.class.getName();

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

    // Database remark comment for a model field (formerly addFieldComment in MBG < 2.0.0,
    // now driven via addFieldAnnotation since MBG 2.0.0 removed the comment hooks).
    private void addColumnRemark(Field field, IntrospectedColumn introspectedColumn) {
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

    // Mark a generated member with the @Generated annotation so the merger treats it as
    // regenerable; custom members added by the user (without the annotation) are preserved.
    private void addGeneratedAnnotation(JavaElement el, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(el, imports, null);
    }

    private void addGeneratedAnnotation(JavaElement el, Set<FullyQualifiedJavaType> imports, String comments) {
        imports.add(GENERATED_ANNOTATION_TYPE);
        el.addAnnotation(buildGeneratedAnnotation(comments));
    }

    private String buildGeneratedAnnotation(String comments) {
        String value = '"' + GENERATED_ANNOTATION_VALUE + '"';
        if (!StringUtil.stringHasValue(comments)) {
            comments = generatedComment;
        }
        if (StringUtil.stringHasValue(comments)) {
            return "@Generated(value = " + value + ", comments = \"" + comments + "\")";
        }
        return "@Generated(" + value + ")";
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

    // Since MBG 2.0.0 the comment hooks were removed and generators mark elements through the
    // annotation hooks instead. We stamp every generated member with the @Generated annotation so
    // that MBG's JavaParser-based file merger can detect and replace them while preserving custom code.
    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(method, imports);
    }

    @Override
    public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(method, imports);
    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(field, imports);
    }

    @Override
    public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(field, imports);
        addColumnRemark(field, introspectedColumn);
    }

    @Override
    public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(innerClass, imports);
    }

    // Marks legacy Example "Criteria" extension points so the merger keeps the user's modifications.
    @Override
    public void addClassAnnotationAndMarkAsDoNotDelete(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(innerClass, imports, MergeConstants.DO_NOT_DELETE_DURING_MERGE);
    }

    @Override
    public void addRecordAnnotation(InnerRecord innerRecord, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(innerRecord, imports);
    }

    @Override
    public void addEnumAnnotation(InnerEnum innerEnum, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {
        addGeneratedAnnotation(innerEnum, imports);
    }
}
