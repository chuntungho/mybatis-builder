/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatementTypeInferrerTest {

    @Test
    public void selectPrefixes() {
        assertEquals("select", StatementTypeInferrer.inferTagName("selectById"));
        assertEquals("select", StatementTypeInferrer.inferTagName("findAll"));
        assertEquals("select", StatementTypeInferrer.inferTagName("getUser"));
        assertEquals("select", StatementTypeInferrer.inferTagName("queryList"));
        assertEquals("select", StatementTypeInferrer.inferTagName("countByName"));
        assertEquals("select", StatementTypeInferrer.inferTagName("listActive"));
    }

    @Test
    public void insertPrefixes() {
        assertEquals("insert", StatementTypeInferrer.inferTagName("insertUser"));
        assertEquals("insert", StatementTypeInferrer.inferTagName("saveBatch"));
        assertEquals("insert", StatementTypeInferrer.inferTagName("addRecord"));
    }

    @Test
    public void updatePrefixes() {
        assertEquals("update", StatementTypeInferrer.inferTagName("updateById"));
        assertEquals("update", StatementTypeInferrer.inferTagName("modifyName"));
    }

    @Test
    public void deletePrefixes() {
        assertEquals("delete", StatementTypeInferrer.inferTagName("deleteById"));
        assertEquals("delete", StatementTypeInferrer.inferTagName("removeAll"));
    }

    @Test
    public void unknownPrefixFallsBackToSelect() {
        assertEquals("select", StatementTypeInferrer.inferTagName("processOne"));
        assertEquals("select", StatementTypeInferrer.inferTagName(""));
        assertEquals("select", StatementTypeInferrer.inferTagName(null));
    }

    @Test
    public void prefixMatchIsCaseInsensitive() {
        assertEquals("select", StatementTypeInferrer.inferTagName("SelectAll"));
        assertEquals("insert", StatementTypeInferrer.inferTagName("Save"));
    }
}
