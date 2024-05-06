/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.view.MybatisBuilderSettingsDialog;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class ManageAction extends DumbAwareAction {
    private static final String ACTION_ID = "MyBatisBuilder.Manage";

    public static AnAction getInstance() {
        return ActionManager.getInstance().getAction(ACTION_ID);
    }

    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        DialogWrapper dialog = new MybatisBuilderSettingsDialog(anActionEvent.getProject());
        dialog.show();
    }
}
