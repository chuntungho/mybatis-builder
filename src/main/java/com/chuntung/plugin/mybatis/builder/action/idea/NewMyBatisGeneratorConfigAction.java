/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class NewMyBatisGeneratorConfigAction extends CreateFileFromTemplateAction implements DumbAware {
    public NewMyBatisGeneratorConfigAction() {
        super(MybatisBuilderBundle.message("action.new.config.text"), MybatisBuilderBundle.message("action.new.config.description"), AllIcons.FileTypes.Xml);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(MybatisBuilderBundle.message("new.config.dialog.title"))
                .addKind(MybatisBuilderBundle.message("new.config.file.kind"), AllIcons.FileTypes.Xml, "MyBatisGenerator.xml");
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, @NotNull String s, String s1) {
        return MybatisBuilderBundle.message("new.config.file.kind");
    }

    @Override
    protected PsiFile createFile(String name, String templateName, PsiDirectory dir) {
        final FileTemplate template = FileTemplateManager.getInstance(dir.getProject()).getJ2eeTemplate(templateName);
        return createFileFromTemplate(name, template, dir);
    }
}
