package com.chuntung.plugin.mybatis.builder.mapper;

import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;

import java.sql.JDBCType;

public final class DemoDynamicSqlSupport {
    public static final Demo demo = new Demo();

    public static final SqlColumn<Integer> id = demo.id;

    public static final SqlColumn<String> name = demo.name;

    public static final class Demo extends SqlTable {
        public final SqlColumn<Integer> id = column("id", JDBCType.INTEGER);

        public final SqlColumn<String> name = column("`name`", JDBCType.VARCHAR);

        public Demo() {
            super("demo");
        }
    }
}