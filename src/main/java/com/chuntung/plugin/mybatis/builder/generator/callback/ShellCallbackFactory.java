/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.callback;

import org.mybatis.generator.api.ShellCallback;

public class ShellCallbackFactory {
    public static ShellCallback createInstance(String runtime) {
        return new JavaMergerShellCallback(true);
    }
}