/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.model;

public enum ColumnActionEnum {
    DEFAULT("Default"), OVERRIDE("Override"), IGNORE("Ignore");

    private String label;

    ColumnActionEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static class Wrapper{
        ColumnActionEnum target;
        Wrapper(ColumnActionEnum actionEnum) {
            target = actionEnum;
        }

        public String toString() {
            return target.getLabel();
        }
    }
}
