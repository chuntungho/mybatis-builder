/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.nio.charset.Charset;

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

    /**
     * Read text from given file with given charset.
     *
     * @param file
     * @param charset
     * @return
     * @throws IOException
     */
    public static String readFromFile(File file, Charset charset) throws IOException {
        Reader reader = new InputStreamReader(new FileInputStream(file), charset);
        return IOUtils.toString(reader);
    }
}
