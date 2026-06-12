/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MapperParamUtilTest {

    @Test
    public void plainName() {
        assertEquals("id", MapperParamUtil.firstSegment("id"));
    }

    @Test
    public void trimsWhitespace() {
        assertEquals("id", MapperParamUtil.firstSegment("  id  "));
    }

    @Test
    public void cutsAtDot() {
        assertEquals("user", MapperParamUtil.firstSegment("user.name"));
    }

    @Test
    public void cutsAtComma() {
        assertEquals("user", MapperParamUtil.firstSegment("user, jdbcType=VARCHAR"));
        assertEquals("name", MapperParamUtil.firstSegment("name,jdbcType=VARCHAR"));
    }

    @Test
    public void cutsAtBracket() {
        assertEquals("list", MapperParamUtil.firstSegment("list[0]"));
    }

    @Test
    public void cutsAtWhitespace() {
        assertEquals("a", MapperParamUtil.firstSegment("a b"));
    }

    @Test
    public void dotBeforeComma() {
        assertEquals("user", MapperParamUtil.firstSegment("user.name, jdbcType=VARCHAR"));
    }

    @Test
    public void nullAndEmpty() {
        assertEquals("", MapperParamUtil.firstSegment(null));
        assertEquals("", MapperParamUtil.firstSegment(""));
        assertEquals("", MapperParamUtil.firstSegment("   "));
    }
}
