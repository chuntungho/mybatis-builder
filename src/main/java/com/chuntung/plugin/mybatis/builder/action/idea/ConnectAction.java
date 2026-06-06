/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.action.TreeNodeLoader;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.function.Supplier;

public class ConnectAction extends DumbAwareAction {
    private final Supplier<TreePath> currentPath;
    private final JTree tree;
    private final TreeNodeLoader loader;

    public ConnectAction(Supplier<TreePath> currentPath, JTree tree, TreeNodeLoader loader) {
        super("Connect");
        this.currentPath = currentPath;
        this.tree = tree;
        this.loader = loader;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DatabaseItem item = connectionItem(currentPath.get());
        e.getPresentation().setEnabledAndVisible(item != null && !item.isConnected());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TreePath path = currentPath.get();
        if (path == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        new Task.Backgroundable(null, "Connecting ...") {
            @Override
            public void run(@NotNull ProgressIndicator pi) {
                loader.load(node, true);
                ApplicationManager.getApplication().invokeLater(() -> tree.expandPath(path));
            }
        }.queue();
    }

    /** Returns the DatabaseItem if the path points to a CONNECTION node, null otherwise. */
    static DatabaseItem connectionItem(TreePath path) {
        if (path == null) return null;
        Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
        if (!(obj instanceof DatabaseItem)) return null;
        DatabaseItem item = (DatabaseItem) obj;
        return DatabaseItem.ItemTypeEnum.CONNECTION.equals(item.getType()) ? item : null;
    }
}
