/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.action.ToolWindowPresenter;
import com.chuntung.plugin.mybatis.builder.action.idea.BuildAction;
import com.chuntung.plugin.mybatis.builder.action.idea.ManageAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * The only entry point of this plugin.
 *
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
        ToolWindowPresenter presenter = ToolWindowPresenter.getInstance(objectTree, project);
        initGUI(presenter, project);
        presenter.initData();
    }

    private void initGUI(ToolWindowPresenter presenter, Project project) {
        // use idea managed toolbar
        DefaultActionGroup actionGroup = new DefaultActionGroup(
                ManageAction.getInstance(),
                new Separator(),
                BuildAction.getInstance(null));
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actionGroup, true);
        setToolbar((JComponent) actionToolbar);
        actionToolbar.setTargetComponent(this);

        // object tree
        objectTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("root"), true));
        objectTree.setRootVisible(false);
        objectTree.setShowsRootHandles(true);

        objectTree.addTreeWillExpandListener(presenter.getTreeWillExpandListener());
        objectTree.addMouseListener(presenter.getMouseListener(project));

        objectTree.setCellRenderer(presenter.getTreeCellRenderer());
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(objectTree, true);
        setContent(scrollPane);
    }
}
