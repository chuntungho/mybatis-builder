/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StatementTypeInferrer {

    public static final String SELECT = "select";
    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";

    private static final Map<String, String> PREFIX_TO_TAG = new LinkedHashMap<>();

    static {
        for (String p : new String[]{"select", "find", "get", "query", "count", "list"}) PREFIX_TO_TAG.put(p, SELECT);
        for (String p : new String[]{"insert", "save", "add"}) PREFIX_TO_TAG.put(p, INSERT);
        for (String p : new String[]{"update", "modify"}) PREFIX_TO_TAG.put(p, UPDATE);
        for (String p : new String[]{"delete", "remove"}) PREFIX_TO_TAG.put(p, DELETE);
    }

    private StatementTypeInferrer() {}

    public static String inferTagName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return SELECT;
        }
        String lower = methodName.toLowerCase();
        for (Map.Entry<String, String> entry : PREFIX_TO_TAG.entrySet()) {
            if (lower.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return SELECT;
    }
}
