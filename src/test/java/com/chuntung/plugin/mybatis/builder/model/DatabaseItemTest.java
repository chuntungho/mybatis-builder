/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DatabaseItemTest {

    @Test
    public void connectionDefaultsToDisconnected() {
        DatabaseItem item = DatabaseItem.of(
                DatabaseItem.ItemTypeEnum.CONNECTION, "conn1", null, "id-1");
        assertFalse(item.isConnected());
    }

    @Test
    public void setConnectedFlipsTheFlag() {
        DatabaseItem item = DatabaseItem.of(
                DatabaseItem.ItemTypeEnum.CONNECTION, "conn1", null, "id-1");

        item.setConnected(true);
        assertTrue(item.isConnected());

        item.setConnected(false);
        assertFalse(item.isConnected());
    }
}
