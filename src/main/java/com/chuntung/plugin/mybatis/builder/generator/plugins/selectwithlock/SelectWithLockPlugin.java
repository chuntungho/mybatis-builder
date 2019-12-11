/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock;

import com.chuntung.plugin.mybatis.builder.util.ConfigUtil;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.*;

/**
 * Support select with lock, add below methods:<br>
 * <ul>
 * <li>selectByPrimaryKeyWithLock</li>
 * <li>selectByExampleWithLock</li>
 * </ul>
 * 
 * {@see org.mybatis.generator.plugins.RowBoundsPlugin}
 *
 * @author Tony Ho
 */
public class SelectWithLockPlugin extends PluginAdapter {
    private SelectWithLockConfig config;
    private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd = new HashMap<>();

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);

        config = ConfigUtil.loadFromProperties(properties, SelectWithLockConfig.class);
    }

    @Override
    public boolean validate(List<String> warnings) {
        boolean passed = true;
        if (Boolean.TRUE.equals(config.byPrimaryKeyWithLockEnabled) && StringUtil.isBlank(config.byPrimaryKeyWithLockOverride)) {
            warnings.add("Please specify method name for select by primary key with lock");
            passed = false;
        }
        if (Boolean.TRUE.equals(config.byExampleWithLockEnabled) && StringUtil.isBlank(config.byExampleWithLockOverride)) {
            warnings.add("Please specify method name for select by example with lock");
            passed = false;
        }
        return passed;
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze,
                                                           IntrospectedTable introspectedTable) {
        if (config.byPrimaryKeyWithLockEnabled && introspectedTable.hasPrimaryKeyColumns()) {
            copyAndAddMethod(method, interfaze, config.byPrimaryKeyWithLockOverride);
        }
        return true;
    }

    @Override
    public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze,
                                                                    IntrospectedTable introspectedTable) {
        if (config.byExampleWithLockEnabled) {
            copyAndAddMethod(method, interfaze, config.byExampleWithLockOverride);
        }
        return true;
    }

    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        List<XmlElement> elements = elementsToAdd.get(introspectedTable.getFullyQualifiedTable());
        if (elements != null) {
            for (XmlElement element : elements) {
                document.getRootElement().addElement(element);
            }
        }

        return true;
    }

    @Override
    public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(
            XmlElement element, IntrospectedTable introspectedTable) {
        if (config.byExampleWithLockEnabled) {
            copyAndSaveElement(element, introspectedTable.getFullyQualifiedTable(), config.byExampleWithLockOverride);
        }
        return true;
    }

    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        if (config.byPrimaryKeyWithLockEnabled) {
            copyAndSaveElement(element, introspectedTable.getFullyQualifiedTable(), config.byPrimaryKeyWithLockOverride);
        }
        return true;
    }

    private void copyAndAddMethod(Method method, Interface interfaze, String newMethodName) {
        Method newMethod = new Method(method);
        newMethod.setName(newMethodName);
        interfaze.addMethod(newMethod);
    }

    private void copyAndSaveElement(XmlElement element, FullyQualifiedTable fqt, String newId) {
        XmlElement newElement = new XmlElement(element);

        // remove old id attribute and add a new one with the new name
        for (Iterator<Attribute> iterator = newElement.getAttributes().iterator(); iterator.hasNext(); ) {
            Attribute attribute = iterator.next();
            if ("id".equals(attribute.getName())) {
                iterator.remove();
                Attribute newAttribute = new Attribute("id", newId);
                newElement.addAttribute(newAttribute);
                break;
            }
        }

        // just for mysql
        newElement.addElement(new TextElement("for update"));

        //  add it to the document later
        List<XmlElement> elements = elementsToAdd.get(fqt);
        if (elements == null) {
            elements = new ArrayList<XmlElement>();
            elementsToAdd.put(fqt, elements);
        }
        elements.add(newElement);
    }
}
