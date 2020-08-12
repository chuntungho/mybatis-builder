/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import org.junit.Assert;
import org.junit.Test;

public class CopyAsExecutableSQLActionTest {

    @Test
    public void resolve() {
        String text1 = "Preparing: select * from test where a=? and b=? \nParameters: 1(Integer), 2(String)";

        System.out.println(text1);
        String sql = CopyAsExecutableSQLAction.resolve(text1);
        Assert.assertEquals("select * from test where a=1 and b='2'", sql);

        String text2 = "Preparing: select * from test where a=?\nParameters: null";
        System.out.println(text2);
        sql = CopyAsExecutableSQLAction.resolve(text2);
        Assert.assertEquals("select * from test where a=null", sql);

        String text3 = "Preparing: select * from test where 1=2\nParameters: ";
        System.out.println(text3);
        sql = CopyAsExecutableSQLAction.resolve(text3);
        Assert.assertEquals("select * from test where 1=2", sql);

        String noParamText = "Preparing: select * from test where a=?";
        sql = CopyAsExecutableSQLAction.resolve(noParamText);
        Assert.assertFalse(sql.isEmpty());

        String multipleText = String.join("\n", text1, text2, text3, noParamText);
        System.out.println(multipleText);
        sql = CopyAsExecutableSQLAction.resolve(multipleText);
        System.out.println(sql);
    }
}