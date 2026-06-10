/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;

import java.sql.Types;
import java.util.Optional;
import java.util.Properties;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;

/**
 * MBG 2.0.0 unconditionally maps temporal JDBC types to JSR-310 java.time types
 * (the default {@link JavaTypeResolverDefaultImpl} no longer reads a property to
 * toggle this). This resolver restores the legacy {@code useJSR310Types} switch:
 * when the property is absent or {@code false}, temporal columns fall back to
 * {@link java.util.Date}.
 *
 * @author Tony Ho
 */
public class JSR310AwareJavaTypeResolver extends JavaTypeResolverDefaultImpl {

    public static final String USE_JSR310_TYPES = "useJSR310Types";

    private static final FullyQualifiedJavaType DATE = new FullyQualifiedJavaType("java.util.Date");

    private boolean useJSR310Types;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.useJSR310Types = isTrue(properties.getProperty(USE_JSR310_TYPES));
    }

    @Override
    public Optional<JdbcTypeInformation> calculateTypeInformation(IntrospectedColumn introspectedColumn) {
        Optional<JdbcTypeInformation> result = super.calculateTypeInformation(introspectedColumn);
        if (!useJSR310Types) {
            switch (introspectedColumn.getJdbcType()) {
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                case Types.TIME_WITH_TIMEZONE:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    return result.map(info -> info.withJavaType(DATE));
                default:
                    break;
            }
        }
        return result;
    }
}
