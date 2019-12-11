/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.plugins;

import com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock.SelectWithLockConfig;
import com.chuntung.plugin.mybatis.builder.util.ConfigUtil;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.*;

/**
 * Add new methods with same name, which are appended with a RowBounds parameter.
 * <p>
 * {@see org.mybatis.generator.plugins.RowBoundsPlugin}
 *
 * @author Tony Ho
 */
public class ExampleRowBoundsPlugin extends PluginAdapter {
    private FullyQualifiedJavaType rowBounds =
            new FullyQualifiedJavaType("org.apache.ibatis.session.RowBounds");

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    IntrospectedTable introspectedTable) {
        copyAndAddMethod(method, interfaze);
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                 IntrospectedTable introspectedTable) {
        copyAndAddMethod(method, interfaze);
        return true;
    }

    private void copyAndAddMethod(Method method, Interface interfaze) {
        Method newMethod = new Method(method);
        newMethod.addParameter(new Parameter(rowBounds, "rowBounds"));
        interfaze.addMethod(newMethod);
        interfaze.addImportedType(rowBounds);
    }
}
