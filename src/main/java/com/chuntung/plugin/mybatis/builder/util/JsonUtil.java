/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtil {
    private static final Gson gson = new GsonBuilder().create();
//    private static final ObjectMapper mapper = new ObjectMapper();
//
//    static {
//        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
//        mapper.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
//        mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
//    }

    public static String toJson(Object bean) {
        try {
//            return mapper.writeValueAsString(bean);
            return gson.toJson(bean);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
//            return mapper.readValue(json, clazz);
            return gson.fromJson(json, clazz);
        } catch (RuntimeException e) {
            return null;
        }
    }

}
