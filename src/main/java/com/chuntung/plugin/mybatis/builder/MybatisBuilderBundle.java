/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class MybatisBuilderBundle extends DynamicBundle {

    @NonNls
    private static final String BUNDLE = "messages.MybatisBuilderBundle";

    private static final MybatisBuilderBundle INSTANCE = new MybatisBuilderBundle();

    private MybatisBuilderBundle() {
        super(BUNDLE);
    }

    public static @NotNull String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key,
                                          Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }
}
