/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator.plugins;

import com.chuntung.plugin.mybatisbuilder.generator.annotation.PluginConfig;
import com.chuntung.plugin.mybatisbuilder.util.ConfigUtil;
import com.chuntung.plugin.mybatisbuilder.util.StringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;

import java.util.List;
import java.util.Properties;

public class RenamePlugin extends PluginAdapter {

    public static final String DOMAIN_NAME = "${domainName}";

    public static class Config {
        public Config() {
        }

        @PluginConfig(displayName = "Mapper type pattern", configKey = "mapperTypePattern", defaultValue = "")
        public String mapperTypePattern;

        @PluginConfig(displayName = "Example type pattern", configKey = "exampleTypePattern", defaultValue = "")
        public String exampleTypePattern;

        @PluginConfig(displayName = "SQL file name pattern", configKey = "sqlFileNamePattern", defaultValue = "")
        public String sqlFileNamePattern;
    }

    private Config config;

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);

        config = ConfigUtil.loadFromProperties(properties, Config.class);
    }

    @Override
    public boolean validate(List<String> warnings) {
        boolean passed = true;
        if (StringUtil.stringHasValue(config.mapperTypePattern) && !config.mapperTypePattern.contains(DOMAIN_NAME)) {
            warnings.add("Mapper type pattern should contain " + DOMAIN_NAME);
            passed = false;
        }

        if (StringUtil.stringHasValue(config.exampleTypePattern) && !config.exampleTypePattern.contains(DOMAIN_NAME)) {
            warnings.add("Example type pattern should contain " + DOMAIN_NAME);
            passed = false;
        }

        if (StringUtil.stringHasValue(config.sqlFileNamePattern) && !config.sqlFileNamePattern.contains(DOMAIN_NAME)) {
            warnings.add("SQL file name pattern should contain " + DOMAIN_NAME);
            passed = false;
        }

        return passed;
    }

    @Override
    public void initialized(IntrospectedTable introspectedTable) {
        String domainObjectName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();

        // rename mapper type
        if (StringUtil.stringHasValue(config.mapperTypePattern)) {
            String mapperType = introspectedTable.getMyBatis3JavaMapperType();
            int packageIdx = mapperType.lastIndexOf('.');
            String packagePrefix = packageIdx > -1 ? mapperType.substring(0, packageIdx + 1) : "";
            String newMapperType = config.mapperTypePattern.replace(DOMAIN_NAME, domainObjectName);
            introspectedTable.setMyBatis3JavaMapperType(packagePrefix + newMapperType);
        }

        // rename example type
        if (StringUtil.stringHasValue(config.exampleTypePattern)) {
            String exampleType = introspectedTable.getExampleType();
            int packageIdx = exampleType.lastIndexOf('.');
            String packagePrefix = packageIdx > -1 ? exampleType.substring(0, packageIdx + 1) : "";
            String newExampleType = config.exampleTypePattern.replace(DOMAIN_NAME, domainObjectName);
            introspectedTable.setExampleType(packagePrefix + newExampleType);
        }

        // rename sql file name
        if (StringUtil.stringHasValue(config.sqlFileNamePattern)) {
            String fileName = introspectedTable.getMyBatis3XmlMapperFileName();
            String newFileName = config.sqlFileNamePattern.replace(DOMAIN_NAME, domainObjectName);
            introspectedTable.setMyBatis3XmlMapperFileName(newFileName);
        }
    }
}
