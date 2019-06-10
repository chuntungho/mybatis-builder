/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator.annotation;

import java.lang.annotation.*;

/**
 * Mybatis Generator plugin configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD})
public @interface PluginConfig {
    String configKey() default "";
    String defaultValue() default "" ;
    String displayName() default "";
    ScopeEnum scope() default ScopeEnum.GLOBAL;
}
