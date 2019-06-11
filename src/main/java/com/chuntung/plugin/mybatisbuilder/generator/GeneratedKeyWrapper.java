/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import com.chuntung.plugin.mybatisbuilder.util.StringUtil;
import org.mybatis.generator.config.GeneratedKey;

/**
 * Use wrapper due to GeneratedKey has no default constructor and setter that can't be persisted.
 */
public class GeneratedKeyWrapper {
    private String column;

    private boolean isIdentity;

    private String statement;

    private String type;

    public GeneratedKeyWrapper() {
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public boolean isIdentity() {
        return isIdentity;
    }

    public void setIdentity(boolean identity) {
        isIdentity = identity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public GeneratedKey createGeneratedKey(TableInfo tableInfo) {
        String keyColumn = getColumn();
        if (StringUtil.isBlank(keyColumn)) {
            keyColumn = tableInfo.getKeyColumn();
        }
        if (StringUtil.stringHasValue(keyColumn)) {
            GeneratedKey generatedKey = new GeneratedKey(keyColumn, statement, isIdentity(), getType());
            return generatedKey;
        } else {
            return null;
        }
    }
}
