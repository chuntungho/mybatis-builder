package com.chuntung.plugin.mybatisbuilder.action.idea;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.NullProgressCallback;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Run Mybatis Generator on xml file.
 */
public class RunMybatisGeneratorAction extends AnAction {
    private NotificationGroup notificationGroup = new NotificationGroup(
            "MybatisBuilder.NotificationGroup",
            NotificationDisplayType.BALLOON, true);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PsiFile psiFile = (PsiFile) event.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null && psiFile instanceof XmlFile) {
            VirtualFile vFile = psiFile.getVirtualFile();
            vFile.refresh(false, false);

            List<String> warnings = new ArrayList<>();
            ConfigurationParser parser = new ConfigurationParser(warnings);
            String error = null;
            try {
                Configuration configuration = parser.parseConfiguration(new File(vFile.getPath()));
                ShellCallback shellCallback = new DefaultShellCallback(true);
                MyBatisGenerator generator = new MyBatisGenerator(configuration, shellCallback, warnings);
                ProgressCallback processCallback = new NullProgressCallback();
                generator.generate(processCallback);

                Notification notification = notificationGroup.createNotification("Generated successfully", NotificationType.INFORMATION);
                Notifications.Bus.notify(notification, event.getProject());

                VirtualFileManager.getInstance().syncRefresh();
            } catch (IOException e) {
                error = e.getMessage();
            } catch (XMLParserException e) {
                error = e.getMessage();
            } catch (InvalidConfigurationException e) {
                error = e.getMessage();
            } catch (InterruptedException e) {
                error = e.getMessage();
            } catch (SQLException e) {
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
