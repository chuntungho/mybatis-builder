/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.action.TreeNodeLoader;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.function.Supplier;

public class RefreshConnectionAction extends DumbAwareAction {
    private final Supplier<TreePath> currentPath;
    private final TreeNodeLoader loader;

    public RefreshConnectionAction(Supplier<TreePath> currentPath, TreeNodeLoader loader) {
        super("Refresh");
        this.currentPath = currentPath;
        this.loader = loader;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TreePath path = currentPath.get();
        if (path == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        new Task.Backgroundable(null, "Loading database objects ...") {
            @Override
            public void run(@NotNull ProgressIndicator pi) {
                loader.load(node, true);
            }
        }.queue();
    }
}
