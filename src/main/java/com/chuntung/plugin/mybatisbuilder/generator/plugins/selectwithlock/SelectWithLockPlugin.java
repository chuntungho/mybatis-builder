package com.chuntung.plugin.mybatisbuilder.generator.plugins.selectwithlock;

import com.chuntung.plugin.mybatisbuilder.util.ConfigUtil;
import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.elements.AbstractXmlElementGenerator;
import org.mybatis.generator.internal.NullProgressCallback;

import java.util.List;
import java.util.Properties;

/**
 * Support select with lock, add below methods:<br>
 * <ul>
 * <li>selectByPrimaryKeyWithLock</li>
 * <li>selectByExampleWithLock</li>
 * </ul>
 *
 * @author Tony Ho
 */
public class SelectWithLockPlugin extends PluginAdapter {
    private SelectWithLockConfig config;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);

        config = ConfigUtil.loadFromProperties(properties, SelectWithLockConfig.class);
    }

    public boolean validate(List<String> warnings) {
        boolean passed = true;
        if (Boolean.TRUE.equals(config.byPrimaryKeyWithLockEnabled) && StringUtils.isBlank(config.byPrimaryKeyWithLockOverride)) {
            warnings.add("Please specify method name for select by primary key with lock");
            passed = false;
        }
        if (Boolean.TRUE.equals(config.byExampleWithLockEnabled) && StringUtils.isBlank(config.byExampleWithLockOverride)) {
            warnings.add("Please specify method name for select by example with lock");
            passed = false;
        }
        return passed;
    }

    @Override
    public boolean clientGenerated(Interface interfaze,
                                   TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
        if (config.byPrimaryKeyWithLockEnabled && introspectedTable.hasPrimaryKeyColumns()) {
            SelectByPrimaryKeyWithLockMethodGenerator methodGenerator = new SelectByPrimaryKeyWithLockMethodGenerator(false, config.byPrimaryKeyWithLockOverride);
            initializeAndExecuteGenerator(methodGenerator, interfaze, introspectedTable);
        }

        if (config.byExampleWithLockEnabled) {
            SelectByExampleWithLockMethoGenerator methodGenerator = new SelectByExampleWithLockMethoGenerator(config.byExampleWithLockOverride);
            initializeAndExecuteGenerator(methodGenerator, interfaze, introspectedTable);
        }

        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        if (config.byPrimaryKeyWithLockEnabled && introspectedTable.hasPrimaryKeyColumns()) {
            SelectByPrimaryKeyWithLockElementGenerator elementGenerator = new SelectByPrimaryKeyWithLockElementGenerator(config.byPrimaryKeyWithLockOverride);
            initializeAndExecuteGenerator(elementGenerator, document.getRootElement(), introspectedTable);
        }

        if (config.byExampleWithLockEnabled) {
            SelectByExampleWithLockElementGenerator elementGenerator = new SelectByExampleWithLockElementGenerator(config.byExampleWithLockOverride);
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
