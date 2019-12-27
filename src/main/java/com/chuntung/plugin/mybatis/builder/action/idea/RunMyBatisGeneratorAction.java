/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.generator.GeneratorToolWrapper;
import com.chuntung.plugin.mybatis.builder.generator.callback.IndicatorProcessCallback;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * Run Mybatis Generator on xml file.
 *
 * @author Tony Ho
 */
public class RunMyBatisGeneratorAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psiFile = (PsiFile) event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null && psiFile instanceof XmlFile) {
            VirtualFile vFile = psiFile.getVirtualFile();
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            Document document = fileDocumentManager.getDocument(vFile);
            fileDocumentManager.saveDocument(document);

            Properties properties = new Properties();
            properties.setProperty("CURRENT_DIR", new File(vFile.getPath()).getParent());
            if (event.getProject() != null) {
                properties.setProperty("PROJECT_DIR", event.getProject().getBasePath());
            }

            new Task.Backgroundable(event.getProject(), "MyBatis Builder") {

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    String error = null;
                    try {
                        List<String> warnings = GeneratorToolWrapper.runWithConfigurationFile(vFile.getPath(), properties, new IndicatorProcessCallback(progressIndicator));
                        String msg = "Generation was done.";
                        if (!warnings.isEmpty()) {
                            msg = msg + "\n" + String.join("\n", warnings);
                        }
                        NotificationHelper.getInstance().notifyInfo(msg, event.getProject());

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
