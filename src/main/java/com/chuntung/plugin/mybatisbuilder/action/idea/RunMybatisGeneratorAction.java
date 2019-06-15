/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.action.idea;

import com.chuntung.plugin.mybatisbuilder.generator.GeneratorToolWrapper;
import com.chuntung.plugin.mybatisbuilder.generator.callback.IndicatorProcessCallback;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Properties;

/**
 * Run Mybatis Generator on xml file.
 *
 * @author Tony Ho
 */
public class RunMybatisGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psiFile = (PsiFile) event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null && psiFile instanceof XmlFile) {
            VirtualFile vFile = psiFile.getVirtualFile();
            vFile.refresh(false, false);

            Properties properties = new Properties();
            properties.setProperty("CURRENT_DIR", new File(vFile.getPath()).getParent());
            if (event.getProject() != null) {
                properties.setProperty("PROJECT_DIR", event.getProject().getBasePath());
            }

            new Task.Backgroundable(event.getProject(), "Mybatis Builder") {

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    String error = null;
                    try {
                        GeneratorToolWrapper.runWithConfigurationFile(vFile.getPath(), properties, new IndicatorProcessCallback(progressIndicator));

                        NotificationHelper.getInstance().notifyInfo("Generated successfully", event.getProject());

                        VirtualFile projectDir = ProjectUtil.guessProjectDir(event.getProject());
                        if (projectDir != null) {
                            VfsUtil.markDirtyAndRefresh(true, true, true, projectDir);
                        }
                    } catch (Exception e) {
                        error = String.valueOf(e.getMessage());
                    }

                    if (error != null) {
                        NotificationHelper.getInstance().notifyError(error, event.getProject());
                    }
                }
            }.queue();


        }
    }

    public void update(@NotNull AnActionEvent event) {
        PsiFile psiFile = (PsiFile) event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null || !(psiFile instanceof XmlFile)) {
            event.getPresentation().setVisible(false);
        }
    }
}
