/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.util;

import org.apache.commons.lang.StringUtils;

public class StringUtil {
    public static boolean stringHasValue(String str) {
        return str != null && !str.isEmpty() && !str.trim().isEmpty();
    }

    public static boolean isBlank(String str) {
        return StringUtils.isBlank(str);
    }

    /**
     * Returns {@code toString()} of the object.
     *
     * @param object
     * @return if the argument is {@code null}, then return {@code null}
     */
    public static String valueOf(Object object) {
        if (object == null) {
            return null;
        } else {
            return object.toString();
        }
    }
}
