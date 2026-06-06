/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.MybatisIcons;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ColoredTreeCellRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class ConnectionTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof DatabaseItem) {
                DatabaseItem item = (DatabaseItem) userObject;
                if (DatabaseItem.ItemTypeEnum.CONNECTION.equals(item.getType())) {
                    Icon base = item.getDriverType() != null
                            ? MybatisIcons.load(item.getDriverType().getIcon())
                            : MybatisIcons.CONNECTION;
                    setIcon(item.isConnected() ? base : IconLoader.getDisabledIcon(base));
                } else if (DatabaseItem.ItemTypeEnum.DATABASE.equals(item.getType())) {
                    setIcon(MybatisIcons.DATABASE);
                } else if (DatabaseItem.ItemTypeEnum.TABLE.equals(item.getType())) {
                    setIcon(MybatisIcons.TABLE);
                    setToolTipText(item.getComment());
                }
                append(item.getName());
            }
        }
    }
}
