/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.util;

import com.chuntung.plugin.mybatisbuilder.generator.annotation.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.Properties;

/**
 * Configuration Utility.
 */
public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    public static <T> T loadFromProperties(Properties properties, Class<T> type) {

        T config = null;
        try {
            config = type.newInstance();
            for (Field field : type.getFields()) {
                PluginConfig annotation = field.getAnnotation(PluginConfig.class);
                if (Modifier.isStatic(field.getModifiers()) || annotation == null) {
                    continue;
                }

                String val = properties.getProperty(annotation.configKey(), annotation.defaultValue());
                field.set(config, convert(val, field.getType()));
            }
        } catch (InstantiationException e) {
            logger.error("Failed to load config from properties", e);
        } catch (IllegalAccessException e) {
            logger.error("Failed to load config from properties", e);
        }

        return config;
    }

    private static Object convert(String val, Class<?> type) {
        if (type.equals(String.class)) {
            return val;
        } else if (Boolean.class.equals(type)) {
            return Boolean.valueOf(val);
        } else if (Integer.class.equals(type)) {
            return Integer.valueOf(val);
        } else if (BigDecimal.class.equals(type)) {
            return new BigDecimal(val);
        }

        return null;
    }
}
