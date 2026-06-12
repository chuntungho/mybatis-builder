/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConnectionTreeReconciliation {

    public static final class Diff {
        public final List<String> toRemove;
        public final Map<String, String> toRename;
        public final List<ConnectionInfo> toAdd;

        Diff(List<String> toRemove, Map<String, String> toRename, List<ConnectionInfo> toAdd) {
            this.toRemove = Collections.unmodifiableList(toRemove);
            this.toRename = Collections.unmodifiableMap(toRename);
            this.toAdd = Collections.unmodifiableList(toAdd);
        }
    }

    public static Diff diff(List<DatabaseItem> existing, List<ConnectionInfo> target) {
        Map<String, ConnectionInfo> targetById = new HashMap<>();
        for (ConnectionInfo info : target) {
            targetById.put(info.getId(), info);
        }

        List<String> toRemove = new ArrayList<>();
        Map<String, String> toRename = new LinkedHashMap<>();
        for (DatabaseItem item : existing) {
            ConnectionInfo match = targetById.get(item.getConnId());
            if (match == null) {
                toRemove.add(item.getConnId());
            } else if (!Objects.equals(item.getName(), match.getName())) {
                toRename.put(item.getConnId(), match.getName());
            }
        }

        List<String> existingIds = new ArrayList<>();
        for (DatabaseItem item : existing) {
            existingIds.add(item.getConnId());
        }
        List<ConnectionInfo> toAdd = new ArrayList<>();
        for (ConnectionInfo info : target) {
            if (!existingIds.contains(info.getId())) {
                toAdd.add(info);
            }
        }

        return new Diff(toRemove, toRename, toAdd);
    }

    private ConnectionTreeReconciliation() {}
}
