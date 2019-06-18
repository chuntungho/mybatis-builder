/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.plugins.selectwithlock;

import com.chuntung.plugin.mybatis.builder.generator.annotation.PluginConfig;
import com.chuntung.plugin.mybatis.builder.generator.annotation.ScopeEnum;

public class SelectWithLockConfig {
    private static final String SELECT_BY_PRIMARY_KEY_WITH_LOCK = "selectByPrimaryKeyWithLock";
    private static final String SELECT_BY_EXAMPLE_WITH_LOCK = "selectByExampleWithLock";

    public static final String BY_PRIMARY_KEY_WITH_LOCK_ENABLED = "byPrimaryKeyWithLockEnabled";
    public static final String BY_EXAMPLE_WITH_LOCK_ENABLED = "byExampleWithLockEnabled";
    public static final String BY_PRIMARY_KEY_WITH_LOCK_OVERRIDE = "byPrimaryKeyWithLockOverride";
    public static final String BY_EXAMPLE_WITH_LOCK_OVERRIDE = "byExampleWithLockOverride";

    @PluginConfig(configKey = BY_PRIMARY_KEY_WITH_LOCK_OVERRIDE, defaultValue = SELECT_BY_PRIMARY_KEY_WITH_LOCK)
    public String byPrimaryKeyWithLockOverride;

    @PluginConfig(configKey = BY_EXAMPLE_WITH_LOCK_OVERRIDE, defaultValue = SELECT_BY_EXAMPLE_WITH_LOCK)
    public String byExampleWithLockOverride;

    @PluginConfig(configKey = BY_PRIMARY_KEY_WITH_LOCK_ENABLED, scope = ScopeEnum.SESSION)
    public Boolean byPrimaryKeyWithLockEnabled;

    @PluginConfig(configKey = BY_EXAMPLE_WITH_LOCK_ENABLED, scope = ScopeEnum.SESSION)
    public Boolean byExampleWithLockEnabled;

    public SelectWithLockConfig() {
        byPrimaryKeyWithLockEnabled = true;
        byExampleWithLockEnabled = false;
    }
}
