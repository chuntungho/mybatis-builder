/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.generator;

import com.chuntung.plugin.mybatisbuilder.generator.annotation.PluginConfig;
import com.chuntung.plugin.mybatisbuilder.generator.callback.CustomShellCallback;
import org.apache.commons.lang.StringUtils;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.api.VerboseProgressCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.NullProgressCallback;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;

/**
 * Mybatis Generator Tool wrapper.
 *
 * @author Tony Ho
 */
public class GeneratorToolWrapper {

    private GeneratorParamWrapper paramWrapper;

    public GeneratorToolWrapper(GeneratorParamWrapper paramWrapper) {
        this.paramWrapper = paramWrapper;
    }

    public List<String> generate() throws InvalidConfigurationException, InterruptedException, SQLException, IOException {
        DefaultParameters defaultParameters = paramWrapper.getDefaultParameters();
        Configuration configuration = new Configuration();
        if (StringUtils.isNotBlank(paramWrapper.getDriverLibrary())) {
            configuration.addClasspathEntry(paramWrapper.getDriverLibrary());
        }
        Context context = new Context(defaultParameters.getDefaultModelType());
        configuration.addContext(context);

        context.setId("mybatis-builder");
        context.setTargetRuntime(defaultParameters.getTargetRuntime());
        context.addProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, defaultParameters.getJavaFileEncoding());

        context.addProperty(PropertyRegistry.CONTEXT_AUTO_DELIMIT_KEYWORDS, "true");
        context.addProperty(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, paramWrapper.getBeginningDelimiter());
        context.addProperty(PropertyRegistry.CONTEXT_ENDING_DELIMITER, paramWrapper.getEndingDelimiter());

        // JDBC config
        context.setJdbcConnectionConfiguration(paramWrapper.getJdbcConfig());

        // java model config,  trim strings
        if (Boolean.TRUE.equals(paramWrapper.getTrimStrings())) {
            paramWrapper.getJavaModelConfig().addProperty(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS, "true");
        }
        context.setJavaModelGeneratorConfiguration(paramWrapper.getJavaModelConfig());

        // java client config
        context.setJavaClientGeneratorConfiguration(paramWrapper.getJavaClientConfig());

        // sql map config
        context.setSqlMapGeneratorConfiguration(paramWrapper.getSqlMapConfig());

        // add each table config
        for (TableInfo tableInfo : paramWrapper.getSelectedTables()) {
            TableConfiguration tableConfig = new TableConfiguration(context);
            populateTableConfig(tableConfig, tableInfo);
            context.addTableConfiguration(tableConfig);
        }

        // comment config
        populateCommentConfig(context);

        // custom plugin
        populatePlugins(context);

        // start invocation
        ShellCallback shellCallback = new CustomShellCallback(true);
        ProgressCallback progressCallback = new VerboseProgressCallback();

        List<String> warnings = new ArrayList<>();
        Set<String> fullyQualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        myBatisGenerator.generate(progressCallback, contexts, fullyQualifiedTables);

        return warnings;
    }

    private void populateCommentConfig(Context context) {
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(CustomCommentGenerator.class.getName());
        commentConfig.addProperty(CustomCommentGenerator.ADD_DATABASE_REMARK, paramWrapper.getDatabaseRemark().toString());
        context.setCommentGeneratorConfiguration(commentConfig);
    }

    private void populatePlugins(Context context) {
        if (paramWrapper.getSelectedPlugins().isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : paramWrapper.getSelectedPlugins().entrySet()) {
            PluginConfiguration pluginConfig = new PluginConfiguration();
            pluginConfig.setConfigurationType(entry.getKey());
            pluginConfig.addProperty("type", entry.getKey());
            populatePluginConfig(entry, pluginConfig);

            context.addPluginConfiguration(pluginConfig);
        }
    }

    private void populatePluginConfig(Map.Entry<String, Object> entry, PluginConfiguration pluginConfig) {
        if (entry.getValue() != null) {
            Object config = entry.getValue();
            for (Field field : config.getClass().getFields()) {
                PluginConfig annotation = field.getAnnotation(PluginConfig.class);
                try {
                    pluginConfig.addProperty(annotation.configKey(), String.valueOf(field.get(config)));
                } catch (IllegalAccessException e) {
                    // NOOP
                }
            }
        }
    }

    private void populateTableConfig(TableConfiguration tableConfig, TableInfo tableInfo) {
        TableConfiguration defaultTableConfig = paramWrapper.getDefaultTableConfig();
        tableConfig.setTableName(tableInfo.getTableName());
        if (StringUtils.isNotBlank(tableInfo.getDomainName())) {
            tableConfig.setDomainObjectName(tableInfo.getDomainName());
        }

        String keyColumn = tableInfo.getKeyColumn();
        if (StringUtils.isBlank(keyColumn)) {
            keyColumn = paramWrapper.getDefaultTabledKey().getColumn();
        }
        if (StringUtils.isNotBlank(keyColumn)) {
            String statement = paramWrapper.getDefaultTabledKey().getStatement();
            if (StringUtils.isBlank(statement)) {
                // use JDBC standard
                statement = "JDBC";
            }
            GeneratedKey generatedKey = new GeneratedKey(keyColumn, statement, true, null);
            tableConfig.setGeneratedKey(generatedKey);
        }

        tableConfig.setSelectByPrimaryKeyQueryId(defaultTableConfig.getSelectByPrimaryKeyQueryId());
        tableConfig.setSelectByExampleQueryId(defaultTableConfig.getSelectByExampleQueryId());

        tableConfig.setSelectByExampleStatementEnabled(defaultTableConfig.isSelectByExampleStatementEnabled());
        tableConfig.setCountByExampleStatementEnabled(defaultTableConfig.isCountByExampleStatementEnabled());
        tableConfig.setUpdateByExampleStatementEnabled(defaultTableConfig.isUpdateByExampleStatementEnabled());
        tableConfig.setDeleteByExampleStatementEnabled(defaultTableConfig.isDeleteByExampleStatementEnabled());

        if (StringUtils.isNotBlank(paramWrapper.getDefaultColumnConfig().getSearchString())) {
            tableConfig.setColumnRenamingRule(paramWrapper.getDefaultColumnConfig());
        }

        String pattern = paramWrapper.getDefaultParameters().getMapperNamePattern();
        if (StringUtils.isNotBlank(pattern)) {
            String mapperName = pattern.replace(DefaultParameters.DOMAIN_NAME_PLACEHOLDER, tableConfig.getDomainObjectName());
            tableConfig.setMapperName(mapperName);
        }
    }

    public static List<String> runWithConfigurationFile(String path) throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser parser = new ConfigurationParser(warnings);
        Configuration configuration = parser.parseConfiguration(new File(path));
        ShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator generator = new MyBatisGenerator(configuration, shellCallback, warnings);
        ProgressCallback processCallback = new NullProgressCallback();
        generator.generate(processCallback);

        return warnings;
    }
}