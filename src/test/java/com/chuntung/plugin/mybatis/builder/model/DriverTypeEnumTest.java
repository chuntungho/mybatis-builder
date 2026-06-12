/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.model;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DriverTypeEnumTest {

    @Test
    public void mysqlDefaultsContainExpectedKeys() {
        Map<String, String> defaults = DriverTypeEnum.MySQL.getDefaultProperties();
        assertEquals("false", defaults.get("useSSL"));
        assertEquals("utf-8", defaults.get("characterEncoding"));
        assertEquals("true", defaults.get("useInformationSchema"));
        assertEquals("true", defaults.get("allowPublicKeyRetrieval"));
        assertEquals("5000", defaults.get("connectTimeout"));
    }

    @Test
    public void postgresqlDefaultsContainLoginTimeout() {
        Map<String, String> defaults = DriverTypeEnum.PostgreSQL.getDefaultProperties();
        assertEquals("5", defaults.get("loginTimeout"));
    }

    @Test
    public void customDefaultsAreEmpty() {
        assertTrue(DriverTypeEnum.Custom.getDefaultProperties().isEmpty());
    }

    @Test
    public void defaultsMapIsUnmodifiable() {
        Map<String, String> defaults = DriverTypeEnum.MySQL.getDefaultProperties();
        try {
            defaults.put("foo", "bar");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void mysqlUrlPatternNoLongerHasQueryString() {
        // Defaults now live in defaultProperties, not the URL pattern.
        assertEquals("jdbc:mysql://${host}:${port}/${db}",
                DriverTypeEnum.MySQL.getUrlPattern());
    }
}
