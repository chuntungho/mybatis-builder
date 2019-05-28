package com.chuntung.plugin.mybatisbuilder.generator.plugins.selectwithlock;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.internal.NullProgressCallback;
import org.mybatis.generator.internal.rules.Rules;

import java.util.List;

/**
 * Support select with lock, add below methods:<br>
 * <ul>
 *     <li>selectByPrimaryKeyWithLock</li>
 *     <li>selectByExampleWithLock</li>
 * </ul>
 *
 * @author Tony Ho
 */
public class SelectWithLockPlugin extends PluginAdapter {
    public static final String SELECT_BY_PRIMARY_KEY_WITH_LOCK = "selectByPrimaryKeyWithLock";
    public static final String SELECT_BY_EXAMPLE_WITH_LOCK = "selectByExampleWithLock";

    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaze,
                                   TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        Rules rules = introspectedTable.getRules();
        if (rules.generateSelectByPrimaryKey()) {
            SelectByPrimaryKeyWithLockMethodGenerator methodGenerator = new SelectByPrimaryKeyWithLockMethodGenerator(false);
            initializeAndExecuteGenerator(methodGenerator, interfaze, introspectedTable);
        }

        if (rules.generateSelectByExampleWithoutBLOBs() || rules.generateSelectByExampleWithBLOBs()) {
            SelectByExampleWithLockMethoGenerator methodGenerator = new SelectByExampleWithLockMethoGenerator();
            initializeAndExecuteGenerator(methodGenerator, interfaze, introspectedTable);
        }

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        Rules rules = introspectedTable.getRules();
        if (rules.generateSelectByPrimaryKey()) {
            SelectByPrimaryKeyWithLockElementGenerator elementGenerator = new SelectByPrimaryKeyWithLockElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, document.getRootElement(), introspectedTable);
        }

        if (rules.generateSelectByExampleWithoutBLOBs() || rules.generateSelectByExampleWithBLOBs()) {
            SelectByExampleWithLockElementGenerator elementGenerator = new SelectByExampleWithLockElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, document.getRootElement(), introspectedTable);
        }

        return true;
    }

    protected void initializeAndExecuteGenerator(
            AbstractJavaMapperMethodGenerator methodGenerator,
            Interface interfaze, IntrospectedTable introspectedTable) {
        methodGenerator.setContext(context);
        methodGenerator.setIntrospectedTable(introspectedTable);
        methodGenerator.setProgressCallback(new NullProgressCallback());
        methodGenerator.setWarnings(null);
        methodGenerator.addInterfaceElements(interfaze);
    }

    protected void initializeAndExecuteGenerator(
            AbstractXmlElementGenerator elementGenerator,
            XmlElement parentElement, IntrospectedTable introspectedTable) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(new NullProgressCallback());
        elementGenerator.setWarnings(null);
        elementGenerator.addElements(parentElement);
    }
}
