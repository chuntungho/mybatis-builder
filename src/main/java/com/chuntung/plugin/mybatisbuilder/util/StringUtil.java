/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.util;

import org.apache.commons.lang.StringUtils;

public class StringUtil {
    public static boolean stringHasValue(String str) {
        return str != null && !str.isEmpty() && !str.trim().isEmpty();
    }

    public static boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }
}
