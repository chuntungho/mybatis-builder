/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import org.junit.Assert;
import org.junit.Test;

public class CopyAsExecutableSQLActionTest {

    @Test
    public void resolve() {
        String text = "Preparing: select * from test where a=? and b=? \nParameters: 1(Integer), 2(String)";

        System.out.println(text);
        String sql = CopyAsExecutableSQLAction.resolve(text);
        Assert.assertEquals("select * from test where a=1 and b='2'", sql);

        text = "Preparing: select * from test where a=?\nParameters: null";
        System.out.println(text);
        sql = CopyAsExecutableSQLAction.resolve(text);
        Assert.assertEquals("select * from test where a=null", sql);

        text = "Preparing: select * from test where 1=2\nParameters: ";
        System.out.println(text);
        sql = CopyAsExecutableSQLAction.resolve(text);
        Assert.assertEquals("select * from test where 1=2", sql);
    }
}