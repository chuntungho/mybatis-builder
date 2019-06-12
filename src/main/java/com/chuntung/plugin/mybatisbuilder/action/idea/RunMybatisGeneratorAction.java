/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.action.idea;

import com.chuntung.plugin.mybatisbuilder.generator.GeneratorToolWrapper;
import com.chuntung.plugin.mybatisbuilder.generator.callback.IndicatorProcessCallback;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.StatusBarProgress;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
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
    private NotificationGroup notificationGroup = new NotificationGroup(
            "MybatisBuilder.NotificationGroup",
            NotificationDisplayType.BALLOON, true);
    private ProgressIndicator processIndicator = new StatusBarProgress();

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

            String error = null;
            try {
                GeneratorToolWrapper.runWithConfigurationFile(vFile.getPath(), properties, new IndicatorProcessCallback(processIndicator));
                Notification notification = notificationGroup.createNotification("Generated successfully", NotificationType.INFORMATION);
                Notifications.Bus.notify(notification, event.getProject());

                VirtualFileManager.getInstance().syncRefresh();
            } catch (Exception e) {
                error = e.getMessage();
            }

            if (error != null) {
                Notification notification = notificationGroup.createNotification(error, NotificationType.ERROR);
                Notifications.Bus.notify(notification, event.getProject());
            }
        }
    }

    public void update(@NotNull AnActionEvent event) {
        PsiFile psiFile = (PsiFile) event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null || !(psiFile instanceof XmlFile)) {
            event.getPresentation().setVisible(false);
        }
    }
}
