/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import com.chuntung.plugin.mybatis.builder.util.ViewUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class ConnectionAction extends DumbAwareAction{
    private ConnectionInfo connectionInfo;
    private JTree objectTree;

    public ConnectionAction(ConnectionInfo connectionInfo, JTree objectTree) {
        super(connectionInfo.getName(), connectionInfo.getDescription(), ViewUtil.getIcon(connectionInfo.getDriverType().getIcon()));
        this.connectionInfo = connectionInfo;
        this.objectTree = objectTree;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) objectTree.getModel().getRoot();

        // check if it existed
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            DatabaseItem item = (DatabaseItem) child.getUserObject();
            if (item.getConnId().equals(connectionInfo.getId())) {
                // select and expand
                TreePath toPath = new TreePath(child.getPath());
                objectTree.setSelectionPath(toPath);
                objectTree.scrollPathToVisible(toPath);
                objectTree.expandPath(toPath);
                return;
            }
        }

        DatabaseItem item = DatabaseItem.of(DatabaseItem.ItemTypeEnum.CONNECTION,
                connectionInfo.getName(),null, connectionInfo.getId());
        root.add(new DefaultMutableTreeNode(item));
        // expand immediately
        TreePath toPath = new TreePath(root.getPath()).pathByAddingChild(root.getLastChild());
        objectTree.setSelectionPath(toPath);
        objectTree.scrollPathToVisible(toPath);
        objectTree.expandPath(toPath);

        objectTree.updateUI();
    }
}
