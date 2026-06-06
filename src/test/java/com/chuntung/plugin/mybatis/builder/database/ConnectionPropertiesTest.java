/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DriverTypeEnum;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConnectionPropertiesTest {

    @Test
    public void emptyUserPropsReturnsDriverDefaults() {
        ConnectionInfo info = new ConnectionInfo();
        info.setDriverType(DriverTypeEnum.MySQL);

        Map<String, String> merged = ConnectionProperties.resolve(info);

        assertEquals("false", merged.get("useSSL"));
        assertEquals("utf-8", merged.get("characterEncoding"));
        assertEquals("true", merged.get("useInformationSchema"));
    }

    @Test
    public void userOverrideBeatsDriverDefault() {
        ConnectionInfo info = new ConnectionInfo();
        info.setDriverType(DriverTypeEnum.MySQL);
        Map<String, String> user = new LinkedHashMap<>();
        user.put("useSSL", "true");
        user.put("connectTimeout", "1");
        info.setProperties(user);

        Map<String, String> merged = ConnectionProperties.resolve(info);

        assertEquals("true", merged.get("useSSL"));
        assertEquals("1", merged.get("connectTimeout"));
        // unrelated defaults still present
        assertEquals("true", merged.get("useInformationSchema"));
    }

    @Test
    public void newKeyFromUserIsIncluded() {
        ConnectionInfo info = new ConnectionInfo();
        info.setDriverType(DriverTypeEnum.MySQL);
        Map<String, String> user = new LinkedHashMap<>();
        user.put("serverTimezone", "UTC");
        info.setProperties(user);

        Map<String, String> merged = ConnectionProperties.resolve(info);

        assertEquals("UTC", merged.get("serverTimezone"));
    }

    @Test
    public void nullUserPropsTreatedAsEmpty() {
        ConnectionInfo info = new ConnectionInfo();
        info.setDriverType(DriverTypeEnum.PostgreSQL);
        info.setProperties(null);

        Map<String, String> merged = ConnectionProperties.resolve(info);

        assertEquals("5", merged.get("loginTimeout"));
    }

    @Test
    public void customDriverWithNoUserPropsHasEmptyResult() {
        ConnectionInfo info = new ConnectionInfo();
        info.setDriverType(DriverTypeEnum.Custom);

        Map<String, String> merged = ConnectionProperties.resolve(info);

        assertEquals(0, merged.size());
    }
}
