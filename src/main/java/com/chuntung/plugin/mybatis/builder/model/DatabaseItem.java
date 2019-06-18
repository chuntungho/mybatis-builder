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

    private String id;
    private String name;
    private ItemTypeEnum type;

    public static DatabaseItem of(ItemTypeEnum type, String name) {
        return new DatabaseItem(type, name, null);
    }

    public static DatabaseItem of(ItemTypeEnum type, String name, String id) {
        return new DatabaseItem(type, name, id);
    }

    private DatabaseItem(ItemTypeEnum type, String name, String id) {
        this.type = type;
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public ItemTypeEnum getType() {
        return this.type;
    }

    // used for tree node
    public String toString() {
        return this.name;
    }

}
