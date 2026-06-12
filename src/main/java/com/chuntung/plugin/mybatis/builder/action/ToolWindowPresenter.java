/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.chuntung.plugin.mybatis.builder.MybatisBuilderTopics;
import com.chuntung.plugin.mybatis.builder.action.idea.BuildAction;
import com.chuntung.plugin.mybatis.builder.action.idea.ConnectAction;
import com.chuntung.plugin.mybatis.builder.action.idea.DisconnectAction;
import com.chuntung.plugin.mybatis.builder.action.idea.RefreshConnectionAction;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import com.chuntung.plugin.mybatis.builder.view.ConnectionTreeCellRenderer;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.PopupHandler;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToolWindowPresenter {
    private MybatisBuilderService service;
    private JTree objectTree;
    private TreePath currentTreePath;

    private final ActionGroup connectionPopupActionGroup;
    private final ActionGroup databasePopupActionGroup;
    private final ActionGroup tablePopupActionGroup;

    public ToolWindowPresenter(JTree objectTree, Project project) {
        service = MybatisBuilderService.getInstance(project);
        this.objectTree = objectTree;

        project.getMessageBus().connect().subscribe(
                MybatisBuilderTopics.CONNECTIONS_CHANGED,
                (ConnectionsChangedListener) () -> ApplicationManager.getApplication().invokeLater(this::reconcileConnectionNodes));

        AnAction connectAction = new ConnectAction(() -> currentTreePath, objectTree, this::loadSubNodes);
        AnAction disconnectAction = new DisconnectAction(() -> currentTreePath, objectTree);
        AnAction refreshAction = new RefreshConnectionAction(() -> currentTreePath, this::loadSubNodes);

        connectionPopupActionGroup = new DefaultActionGroup(
                connectAction,
                disconnectAction,
                new Separator(),
                refreshAction);

        databasePopupActionGroup = new DefaultActionGroup(refreshAction);

        AnAction buildAction = BuildAction.getInstance(project);
        tablePopupActionGroup = new DefaultActionGroup(buildAction);
    }

    public static ToolWindowPresenter getInstance(JTree objectTree, Project project) {
        return new ToolWindowPresenter(objectTree, project);
    }

    public void initData() {
        ApplicationManager.getApplication().invokeLater(this::loadConnectionNodes);
    }

    private synchronized void reconcileConnectionNodes() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) objectTree.getModel().getRoot();

        List<ConnectionInfo> all = service.loadConnectionInfoList();
        List<ConnectionInfo> activeList = new ArrayList<>();
        if (all != null) {
            for (ConnectionInfo info : all) {
                if (Boolean.TRUE.equals(info.getActive())) {
                    activeList.add(info);
                }
            }
        }

        List<DatabaseItem> existing = new ArrayList<>();
        Map<String, DefaultMutableTreeNode> existingNodes = new LinkedHashMap<>();
        for (int i = 0; i < root.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
            Object userObject = child.getUserObject();
            if (userObject instanceof DatabaseItem) {
                DatabaseItem item = (DatabaseItem) userObject;
                if (DatabaseItem.ItemTypeEnum.CONNECTION.equals(item.getType())) {
                    existing.add(item);
                    existingNodes.put(item.getConnId(), child);
                }
            }
        }

        ConnectionTreeReconciliation.Diff diff =
                ConnectionTreeReconciliation.diff(existing, activeList);

        for (String connId : diff.toRemove) {
            DefaultMutableTreeNode node = existingNodes.get(connId);
            if (node != null) {
                root.remove(node);
            }
        }
        for (Map.Entry<String, String> rename : diff.toRename.entrySet()) {
            DefaultMutableTreeNode node = existingNodes.get(rename.getKey());
            if (node != null) {
                DatabaseItem old = (DatabaseItem) node.getUserObject();
                DatabaseItem renamed = DatabaseItem.of(
                        DatabaseItem.ItemTypeEnum.CONNECTION,
                        rename.getValue(), old.getComment(), old.getConnId());
                renamed.setConnected(old.isConnected());
                renamed.setDriverType(old.getDriverType());
                node.setUserObject(renamed);
            }
        }
        for (ConnectionInfo info : diff.toAdd) {
            DatabaseItem item = DatabaseItem.of(
                    DatabaseItem.ItemTypeEnum.CONNECTION,
                    info.getName(), null, info.getId());
            item.setDriverType(info.getDriverType());
            root.add(new DefaultMutableTreeNode(item, true));
        }

        ((DefaultTreeModel) objectTree.getModel()).nodeStructureChanged(root);
    }

    private synchronized void loadConnectionNodes() {
        List<ConnectionInfo> connectionInfoList = service.loadConnectionInfoList();
        if (connectionInfoList == null) {
            return;
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) objectTree.getModel().getRoot();
        for (ConnectionInfo connectionInfo : connectionInfoList) {
            if (Boolean.TRUE.equals(connectionInfo.getActive())) {
                DatabaseItem item = DatabaseItem.of(DatabaseItem.ItemTypeEnum.CONNECTION,
                        connectionInfo.getName(), null, connectionInfo.getId());
                item.setDriverType(connectionInfo.getDriverType());
                root.add(new DefaultMutableTreeNode(item, true));
            }
        }
        objectTree.updateUI();
    }

    private synchronized void loadSubNodes(DefaultMutableTreeNode node, boolean forced) {
        int count = node.getChildCount();
        if (forced || count == 0) {
            node.removeAllChildren();

            try {
                DatabaseItem item = (DatabaseItem) node.getUserObject();

                if (DatabaseItem.ItemTypeEnum.CONNECTION.equals(item.getType())) {

                    ConnectionInfo connectionInfo = service.getConnectionInfoWithPassword(item.getConnId());
                    String defaultDatabase = connectionInfo.getDatabase();

                    List<DatabaseItem> databaseItems = service.fetchDatabases(item.getConnId());
                    ApplicationManager.getApplication().invokeLater(() -> {
                        item.setConnected(true);
                        TreePath toPath = null;
                        for (DatabaseItem dbItem : databaseItems) {
                            node.add(new DefaultMutableTreeNode(dbItem, true));
                            if (dbItem.getName().equals(defaultDatabase)) {
                                toPath = new TreePath(node.getPath()).pathByAddingChild(node.getLastChild());
                            }
                        }

                        if (toPath != null) {
                            objectTree.setSelectionPath(toPath);
                            objectTree.scrollPathToVisible(toPath);
                        }
                    });
                } else if (DatabaseItem.ItemTypeEnum.DATABASE.equals(item.getType())) {
                    DefaultMutableTreeNode connNode = (DefaultMutableTreeNode) node.getParent();
                    String connectionId = ((DatabaseItem) connNode.getUserObject()).getConnId();
                    List<DatabaseItem> databaseItems = service.fetchTables(connectionId, item.getName());
                    ApplicationManager.getApplication().invokeLater(() -> {
                        for (DatabaseItem dbItem : databaseItems) {
                            node.add(new DefaultMutableTreeNode(dbItem, false));
                        }
                    });
                }
            } catch (SQLException e) {
                String message = e.getMessage();
                if (e.getCause() instanceof java.net.UnknownHostException) {
                    message = "Unknown host: " + e.getCause().getMessage();
                }
                final String finalMessage = message;
                ApplicationManager.getApplication().invokeLater(
                        () -> Messages.showErrorDialog(finalMessage, "Database Error"));
            } finally {
                ApplicationManager.getApplication().invokeLater(objectTree::updateUI);
            }
        }
    }

    public TreeWillExpandListener getTreeWillExpandListener() {
        return new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (node.getUserObject() instanceof DatabaseItem) {
                    new Task.Backgroundable(null, "Loading database objects ...") {
                        @Override
                        public void run(@NotNull ProgressIndicator progressIndicator) {
                            loadSubNodes(node, false);
                        }
                    }.queue();
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // NOOP
            }
        };
    }

    public PopupHandler getMouseListener(Project project) {
        return new PopupHandler() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTree source = (JTree) e.getSource();
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) source.getLastSelectedPathComponent();
                    DatabaseItem item = (DatabaseItem) node.getUserObject();
                    if (DatabaseItem.ItemTypeEnum.TABLE.equals(item.getType())) {
                        ActionUtil.invokeAction(BuildAction.getInstance(project), source, ActionPlaces.UNKNOWN, e, null);
                    }
                }
                super.mouseClicked(e);
            }

            @Override
            public void invokePopup(Component comp, int x, int y) {
                JTree source = (JTree) comp;
                TreePath path = source.getClosestPathForLocation(x, y);

                currentTreePath = path;
                if (path == null) return;

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                DatabaseItem item = (DatabaseItem) node.getUserObject();

                ActionGroup popupActionGroup = null;
                if (DatabaseItem.ItemTypeEnum.CONNECTION.equals(item.getType())) {
                    popupActionGroup = connectionPopupActionGroup;
                } else if (DatabaseItem.ItemTypeEnum.DATABASE.equals(item.getType())) {
                    popupActionGroup = databasePopupActionGroup;
                } else if (DatabaseItem.ItemTypeEnum.TABLE.equals(item.getType())) {
                    popupActionGroup = tablePopupActionGroup;
                }

                if (popupActionGroup != null) {
                    ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", popupActionGroup);
                    JPopupMenu popupMenu = actionPopupMenu.getComponent();
                    popupMenu.show(source, x, y);
                }
            }
        };
    }

    public TreeCellRenderer getTreeCellRenderer() {
        return new ConnectionTreeCellRenderer();
    }
}
