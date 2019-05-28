package com.chuntung.plugin.mybatisbuilder.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * Lombok Data Annotation support.
 *
 * @author Tony Ho
 */
public class LombokPlugin extends PluginAdapter {
    private final static String ANNOTATION = "@Data";
    private final static FullyQualifiedJavaType ANNOTATION_TYPE = new FullyQualifiedJavaType("lombok.Data");

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    private void populateLombokAnnotation(TopLevelClass topLevelClass) {
        topLevelClass.addAnnotation(ANNOTATION);
        topLevelClass.addImportedType(ANNOTATION_TYPE);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        populateLombokAnnotation(topLevelClass);
        return true;
    }


    @Override
    public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
                                                 IntrospectedTable introspectedTable) {
        populateLombokAnnotation(topLevelClass);
        return true;
    }

    @Override
    public boolean modelRecordWithBLOBsClassGenerated(
            TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        populateLombokAnnotation(topLevelClass);
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        return false;
    }


    @Override
    public boolean modelSetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        return false;
    }

}
