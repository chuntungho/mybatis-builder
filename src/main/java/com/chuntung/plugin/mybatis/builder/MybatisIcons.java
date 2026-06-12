/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public final class MybatisIcons {
    public static final Icon MYBATIS    = load("/images/popupMenuMybatis.svg");
    public static final Icon CONNECTION = load("/images/connection.png");
    public static final Icon CONNECTION_DISCONNECTED = IconLoader.getDisabledIcon(CONNECTION);
    public static final Icon DATABASE   = load("/images/database.png");
    public static final Icon TABLE      = load("/images/table.png");
    public static final Icon BUILD      = load("/images/build.svg");
    public static final Icon MAPPER_TO_XML = load("/images/mapperToXml.svg");
    public static final Icon XML_TO_MAPPER = load("/images/xmlToMapper.svg");

    public static Icon load(String path) {
        return IconLoader.getIcon(path, MybatisIcons.class);
    }

    private MybatisIcons() {}
}
