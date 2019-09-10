/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder;

import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.openapi.util.IconLoader;

public class MyBatisBuilderFileTemplateProvider implements FileTemplateGroupDescriptorFactory {

    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("MyBatis Builder", IconLoader.getIcon("/images/mybatis-icon.png"));
        group.addTemplate(new FileTemplateDescriptor("MyBatisGenerator.xml", AllIcons.FileTypes.Xml));
        return group;
    }
}
