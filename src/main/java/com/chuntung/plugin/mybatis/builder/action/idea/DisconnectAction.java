/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderBundle;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.function.Supplier;

public class DisconnectAction extends DumbAwareAction {
    private final Supplier<TreePath> currentPath;
    private final JTree tree;

    public DisconnectAction(Supplier<TreePath> currentPath, JTree tree) {
        super(MybatisBuilderBundle.message("action.disconnect.text"));
        this.currentPath = currentPath;
        this.tree = tree;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DatabaseItem item = ConnectAction.connectionItem(currentPath.get());
        e.getPresentation().setEnabledAndVisible(item != null && item.isConnected());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TreePath path = currentPath.get();
        if (path == null) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
        DatabaseItem item = (DatabaseItem) node.getUserObject();
        tree.collapsePath(new TreePath(node.getPath()));
        node.removeAllChildren();
        item.setConnected(false);
        ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
    }
}
