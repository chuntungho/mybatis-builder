/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConnectionTreeReconciliationTest {

    private DatabaseItem connectionItem(String connId, String name) {
        return DatabaseItem.of(DatabaseItem.ItemTypeEnum.CONNECTION, name, null, connId);
    }

    private ConnectionInfo connection(String id, String name) {
        ConnectionInfo info = new ConnectionInfo();
        info.setId(id);
        info.setName(name);
        info.setActive(Boolean.TRUE);
        return info;
    }

    @Test
    public void emptyInputsProduceEmptyDiff() {
        ConnectionTreeReconciliation.Diff diff = ConnectionTreeReconciliation.diff(
                Collections.emptyList(), Collections.emptyList());
        assertTrue(diff.toRemove.isEmpty());
        assertTrue(diff.toRename.isEmpty());
        assertTrue(diff.toAdd.isEmpty());
    }

    @Test
    public void identicalListsProduceEmptyDiff() {
        List<DatabaseItem> existing = Arrays.asList(
                connectionItem("a", "Alpha"),
                connectionItem("b", "Beta"));
        List<ConnectionInfo> target = Arrays.asList(
                connection("a", "Alpha"),
                connection("b", "Beta"));

        ConnectionTreeReconciliation.Diff diff = ConnectionTreeReconciliation.diff(existing, target);

        assertTrue(diff.toRemove.isEmpty());
        assertTrue(diff.toRename.isEmpty());
        assertTrue(diff.toAdd.isEmpty());
    }

    @Test
    public void existingNotInTargetIsRemoved() {
        List<DatabaseItem> existing = Arrays.asList(
                connectionItem("a", "Alpha"),
                connectionItem("b", "Beta"));
        List<ConnectionInfo> target = Collections.singletonList(connection("a", "Alpha"));

        ConnectionTreeReconciliation.Diff diff = ConnectionTreeReconciliation.diff(existing, target);

        assertEquals(Collections.singletonList("b"), diff.toRemove);
        assertTrue(diff.toRename.isEmpty());
        assertTrue(diff.toAdd.isEmpty());
    }

    @Test
    public void targetNotInExistingIsAdded() {
        List<DatabaseItem> existing = Collections.singletonList(connectionItem("a", "Alpha"));
        ConnectionInfo newOne = connection("c", "Gamma");
        List<ConnectionInfo> target = Arrays.asList(connection("a", "Alpha"), newOne);

        ConnectionTreeReconciliation.Diff diff = ConnectionTreeReconciliation.diff(existing, target);

        assertTrue(diff.toRemove.isEmpty());
        assertTrue(diff.toRename.isEmpty());
        assertEquals(1, diff.toAdd.size());
        assertEquals("c", diff.toAdd.get(0).getId());
        assertEquals("Gamma", diff.toAdd.get(0).getName());
    }

    @Test
    public void renameIsReported() {
        List<DatabaseItem> existing = Collections.singletonList(connectionItem("a", "Alpha"));
        List<ConnectionInfo> target = Collections.singletonList(connection("a", "Alpha Renamed"));

        ConnectionTreeReconciliation.Diff diff = ConnectionTreeReconciliation.diff(existing, target);

        assertTrue(diff.toRemove.isEmpty());
        assertTrue(diff.toAdd.isEmpty());
        assertEquals(Collections.singletonMap("a", "Alpha Renamed"), diff.toRename);
    }

    @Test
    public void mixedScenarioReportsAllThree() {
        List<DatabaseItem> existing = Arrays.asList(
                connectionItem("a", "Alpha"),
                connectionItem("b", "Beta"));
        List<ConnectionInfo> target = Arrays.asList(
                connection("a", "Alpha Renamed"),
                connection("c", "Gamma"));

        ConnectionTreeReconciliation.Diff diff = ConnectionTreeReconciliation.diff(existing, target);

        assertEquals(Collections.singletonList("b"), diff.toRemove);
        assertEquals(Collections.singletonMap("a", "Alpha Renamed"), diff.toRename);
        assertEquals(1, diff.toAdd.size());
        assertEquals("c", diff.toAdd.get(0).getId());
    }
}
