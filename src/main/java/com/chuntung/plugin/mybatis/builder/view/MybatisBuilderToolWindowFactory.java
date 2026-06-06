/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.MybatisIcons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class MybatisBuilderToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(MybatisIcons.MYBATIS);
    }


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        MybatisBuilderToolWindowPanel mybatisBuilderWindow = new MybatisBuilderToolWindowPanel(project);

        Content content = ContentFactory.getInstance().createContent(mybatisBuilderWindow.getComponent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
