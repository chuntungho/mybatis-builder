/*
 * Copyright (c) 2019-2024 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

/**
 * Simple mutable renaming rule bean, replaces MBG's immutable DomainObjectRenamingRule / ColumnRenamingRule.
 */
public class RenamingRule {
    private String searchString = "";
    private String replaceString = "";

    public RenamingRule() {
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString == null ? "" : searchString;
    }

    public String getReplaceString() {
        return replaceString;
    }

    public void setReplaceString(String replaceString) {
        this.replaceString = replaceString == null ? "" : replaceString;
    }
}
