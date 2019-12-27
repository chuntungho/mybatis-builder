/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.action.ObjectTreeHandler;
import com.chuntung.plugin.mybatis.builder.action.idea.BuildAction;
import com.chuntung.plugin.mybatis.builder.action.idea.PopupAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * The only entry point of this plugin.
 *
 * @author Tony Ho
 */
public class MybatisBuilderToolWindowPanel extends SimpleToolWindowPanel {
    // NOTE: should be consistent with plugin.xml defined
    public static final String WINDOW_ID = "MyBatis Builder";

    private SimpleTree objectTree = new SimpleTree();

    public JTree getTree() {
        return objectTree;
    }

    public MybatisBuilderToolWindowPanel(Project project) {
        super(true, true);
        ObjectTreeHandler treeHandler = ObjectTreeHandler.getInstance(objectTree, project);
        initGUI(treeHandler);
        treeHandler.initData();
    }

    private void initGUI(ObjectTreeHandler treeHandler) {
        // use idea managed toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup(
                new PopupAction(objectTree),
                new Separator(),
                BuildAction.getInstance(null));
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("", actionGroup, true);
        setToolbar((JComponent) actionToolbar);

        // object tree
        objectTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root"), true));
        objectTree.setRootVisible(false);
        objectTree.setShowsRootHandles(true);

        objectTree.addTreeWillExpandListener(treeHandler.getTreeWillExpandListener());
        objectTree.addMouseListener(treeHandler.getMouseListener());

        objectTree.setCellRenderer(treeHandler.getTreeCellRenderer());

        JBScrollPane scrollPane = new JBScrollPane(objectTree);
        setContent(scrollPane);
    }
}
