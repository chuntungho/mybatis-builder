/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

/**
 * The User Object of JTree
 *
 * @author Tony Ho
 */
public class DatabaseItem {
    public enum ItemTypeEnum {
        CONNECTION, DATABASE, TABLE
    }

    private String connId;
    private String name;
    private String comment;
    private ItemTypeEnum type;

    public static DatabaseItem of(ItemTypeEnum type, String name) {
        return new DatabaseItem(type, name, null, null);
    }

    public static DatabaseItem of(ItemTypeEnum type, String name, String comment, String id) {
        return new DatabaseItem(type, name, comment, id);
    }

    private DatabaseItem(ItemTypeEnum type, String name, String comment, String connId) {
        this.type = type;
        this.name = name;
        this.comment = comment;
        this.connId = connId;
    }

    public String getConnId() {
        return this.connId;
    }

    public String getName() {
        return this.name;
    }

    public ItemTypeEnum getType() {
        return this.type;
    }

    public String getComment() {
        return comment;
    }
}
