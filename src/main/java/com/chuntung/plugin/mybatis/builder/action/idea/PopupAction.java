/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.List;

public class PopupAction extends DumbAwareAction {

    private JTree objectTree;

    public PopupAction(JTree objectTree) {
        super("Connections", null, IconLoader.getIcon("/images/connections.png"));
        this.objectTree = objectTree;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        populate(actionGroup, e.getProject());

        InputEvent inputEvent = e.getInputEvent();
        if (inputEvent != null) {
            Component component = (Component) inputEvent.getSource();
            if (component.isShowing()) {
                ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                        "",
                        actionGroup,
                        e.getDataContext(),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true,
                        null,
                        10);
                showBelowComponent(popup, component);
            }
        }
    }

    // dynamically
    private void populate(DefaultActionGroup actionGroup, Project project) {
        MybatisBuilderService service = MybatisBuilderService.getInstance(project);
        List<ConnectionInfo> connectionInfos = service.loadConnectionInfoList();
        if (connectionInfos != null && connectionInfos.size() > 0) {
            for (ConnectionInfo connectionInfo : connectionInfos) {
                if (Boolean.TRUE.equals(connectionInfo.getActive())) {
                    AnAction action = new ConnectionAction(connectionInfo, objectTree);
                    actionGroup.add(action);
                }
            }
            if (actionGroup.getChildrenCount() > 0) {
                actionGroup.addSeparator();
            }
        }

        actionGroup.add(ManageAction.getInstance());
    }

    private void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point((int) (locationOnScreen.getX() + 10),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }
}
