/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.chuntung.plugin.mybatis.builder.action.idea.NotificationHelper;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorToolWrapper;
import com.chuntung.plugin.mybatis.builder.generator.callback.IndicatorProcessCallback;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ParametersHandler {
    private static final Logger logger = LoggerFactory.getLogger(ParametersHandler.class);

    private final MybatisBuilderService service;

    public ParametersHandler(Project project) {
        service = MybatisBuilderService.getInstance(project);
    }

    public static ParametersHandler getInstance(Project project) {
        return new ParametersHandler(project);
    }

    public void stashGeneratorParamWrapper(GeneratorParamWrapper paramWrapper) {
        service.stashGeneratorParamWrapper(paramWrapper);
    }

    public void exportConfiguration(GeneratorParamWrapper paramWrapper, File file, Project project) {
        try {
            new GeneratorToolWrapper(paramWrapper, null).export(file);
            NotificationHelper.getInstance().notifyInfo("Exported to " + file.getAbsolutePath(), project);
        } catch (IOException ex) {
            NotificationHelper.getInstance().notifyError(ex.getMessage(), project);
        }
    }

    public void generate(GeneratorParamWrapper paramWrapper, Project project) {
        // stash last parameters
        service.stashGeneratorParamWrapper(paramWrapper);

        new Task.Backgroundable(project, "MyBatis Builder") {

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                IndicatorProcessCallback processCallback = new IndicatorProcessCallback(progressIndicator);
                GeneratorToolWrapper toolWrapper = new GeneratorToolWrapper(paramWrapper, processCallback);
                try {
                    List<String> warnings = toolWrapper.generate();

                    VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
                    if (projectDir != null) {
                        VfsUtil.markDirtyAndRefresh(true, true, true, projectDir);
                    }

                    int cnt = paramWrapper.getSelectedTables().size();
                    String msg = "Generation for " + cnt + (cnt > 1 ? " tables" : " table") + " was done.";
                    if (!warnings.isEmpty()) {
                        msg = msg + "\n" + String.join("\n", warnings);
                    }
                    NotificationHelper.getInstance().notifyInfo(msg, project);
                } catch (Exception e) {
                    logger.warn("Failed to generate", e);
                    NotificationHelper.getInstance().notifyError(String.valueOf(e.getMessage()), project);
                }
            }
        }.queue();
    }

}
