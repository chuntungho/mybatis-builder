/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action;

import javax.swing.tree.DefaultMutableTreeNode;

@FunctionalInterface
public interface TreeNodeLoader {
    void load(DefaultMutableTreeNode node, boolean forced);
}
