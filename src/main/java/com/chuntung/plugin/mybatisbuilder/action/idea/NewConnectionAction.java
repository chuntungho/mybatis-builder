/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.action.idea;

import com.chuntung.plugin.mybatisbuilder.view.MybatisBuilderSettingsDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

public class NewConnectionAction extends AnAction {
    public NewConnectionAction() {
        super("Manage...", "Create new connection", IconLoader.getIcon("/images/new.png"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        DialogWrapper dialog = new MybatisBuilderSettingsDialog(anActionEvent.getProject());
        dialog.show();
    }
}
