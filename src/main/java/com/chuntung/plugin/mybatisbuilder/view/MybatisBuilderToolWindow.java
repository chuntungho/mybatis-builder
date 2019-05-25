/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.view;

import com.chuntung.plugin.mybatisbuilder.action.ObjectTreeHandler;
import com.chuntung.plugin.mybatisbuilder.action.idea.BuildAction;
import com.chuntung.plugin.mybatisbuilder.action.idea.PopupAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * The only enter point of this plugin.
 *
 * @author Tony Ho
 */
public class MybatisBuilderToolWindow {
    private JPanel mainPanel;
    private JPanel actionPanel;
    private JTree objectTree;

    public MybatisBuilderToolWindow(Project project) {
        initGUI(project);
    }

    private void initGUI(Project project) {
        // tool bar
        // it seems that swing toolbar style has no effect in tool window,
        // the only way is to use idea managed toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup(
                new PopupAction(objectTree),
                new Separator(),
                new BuildAction(objectTree));
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("", actionGroup, true);
        actionToolbar.setTargetComponent(actionPanel);
        actionPanel.add(actionToolbar.getComponent());

        // object tree
        ObjectTreeHandler treeHandler = ObjectTreeHandler.getInstance(objectTree, project);
        objectTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root"), true));
        objectTree.setRootVisible(false);
        objectTree.setShowsRootHandles(true);

        objectTree.addTreeWillExpandListener(treeHandler.getTreeWillExpandListener());
        objectTree.addMouseListener(treeHandler.getMouseListener());

        objectTree.setCellRenderer(treeHandler.getTreeCellRenderer());

        // init tree nodes
        treeHandler.initConnectionNodes();
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
