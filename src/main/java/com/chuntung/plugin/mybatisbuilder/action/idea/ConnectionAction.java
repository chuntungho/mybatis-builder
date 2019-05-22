/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.action.idea;

import com.chuntung.plugin.mybatisbuilder.model.ConnectionInfo;
import com.chuntung.plugin.mybatisbuilder.model.DatabaseItem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class ConnectionAction extends AnAction {
    private ConnectionInfo connectionInfo;
    private JTree objectTree;

    public ConnectionAction(ConnectionInfo connectionInfo, JTree objectTree) {
        super(connectionInfo.getName(), connectionInfo.getDescription(), IconLoader.getIcon(connectionInfo.getDriverType().getIcon()));
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
            if (item.getId().equals(connectionInfo.getId())) {
                objectTree.setSelectionPath(new TreePath(child.getPath()));
                return;
            }
        }

        DatabaseItem item = DatabaseItem.of(DatabaseItem.ItemTypeEnum.CONNECTION,
                connectionInfo.getName(), connectionInfo.getId());
        root.add(new DefaultMutableTreeNode(item));
        objectTree.updateUI();
    }
}
