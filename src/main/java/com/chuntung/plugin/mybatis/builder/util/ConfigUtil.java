/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.util;

import com.chuntung.plugin.mybatis.builder.generator.annotation.PluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration Utility.
 */
public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static Map<Class, Map<String, Field>> fieldCache = new HashMap();

    public static <T> T loadFromProperties(Properties properties, Class<T> type) {
        initTypeFields(type);
        T config = null;
        try {
            config = type.newInstance();
            Map<String, Field> fieldMap = fieldCache.get(type);
            for (Field field : fieldMap.values()) {
                PluginConfig annotation = field.getAnnotation(PluginConfig.class);
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

    public static Object getFieldValueByConfigKey(Object bean, String configKey) {
        initTypeFields(bean.getClass());
        Map<String, Field> fieldMap = fieldCache.get(bean.getClass());
        Field field = fieldMap.get(configKey);
        if (field == null) {
            return null;
        }

        Object val = null;
        try {
            val = field.get(bean);
        } catch (IllegalAccessException e) {
            logger.error("Failed to get field value", e);
        }

        // load default value from annotation
        if (val == null || (val instanceof String && StringUtil.isBlank(val.toString()))) {
            val = convert(field.getAnnotation(PluginConfig.class).defaultValue(), field.getType());
        }

        return val;
    }

    private static <T> void initTypeFields(Class<T> type) {
        if (!fieldCache.containsKey(type)) {
            Map<String, Field> fieldMap = new HashMap<>();
            for (Field field : type.getFields()) {
                PluginConfig annotation = field.getAnnotation(PluginConfig.class);
                if (Modifier.isStatic(field.getModifiers()) || annotation == null) {
                    continue;
                }
                fieldMap.put(annotation.configKey(), field);
            }
            fieldCache.put(type, fieldMap);
        }
    }

    private static Object convert(String val, Class<?> type) {
        if (type.equals(String.class)) {
            return val;
        }

        if (StringUtil.isBlank(val)) {
            return null;
        }

        if (Boolean.class.equals(type)) {
            return Boolean.valueOf(val);
        } else if (Integer.class.equals(type)) {
            return Integer.valueOf(val);
        } else if (BigDecimal.class.equals(type)) {
            return new BigDecimal(val);
        }

        return null;
    }
}
