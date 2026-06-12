/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder;

import com.chuntung.plugin.mybatis.builder.action.ConnectionsChangedListener;
import com.intellij.util.messages.Topic;

public final class MybatisBuilderTopics {

    public static final Topic<ConnectionsChangedListener> CONNECTIONS_CHANGED =
            Topic.create("MyBatis Builder connections changed",
                    ConnectionsChangedListener.class);

    private MybatisBuilderTopics() {}
}
