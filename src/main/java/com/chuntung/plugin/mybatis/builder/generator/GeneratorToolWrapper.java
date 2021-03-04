/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.generator.annotation.PluginConfig;
import com.chuntung.plugin.mybatis.builder.generator.callback.ShellCallbackFactory;
import com.chuntung.plugin.mybatis.builder.generator.plugins.DsqlRuntimePatchPlugin;
import com.chuntung.plugin.mybatis.builder.generator.plugins.RenamePlugin;
import com.chuntung.plugin.mybatis.builder.model.ColumnActionEnum;
import com.chuntung.plugin.mybatis.builder.model.ColumnInfo;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.plugins.dsql.DisableDeletePlugin;
import org.mybatis.generator.plugins.dsql.DisableInsertPlugin;
import org.mybatis.generator.plugins.dsql.DisableUpdatePlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.*;

/**
 * Mybatis Generator Tool wrapper.
 *
 * @author Tony Ho
 */
public class GeneratorToolWrapper {

    private GeneratorParamWrapper paramWrapper;
    private ProgressCallback progressCallback;

    public GeneratorToolWrapper(GeneratorParamWrapper paramWrapper, ProgressCallback progressCallback) {
        this.paramWrapper = paramWrapper;
        this.progressCallback = progressCallback;
    }

    public List<String> generate() throws InvalidConfigurationException, InterruptedException, SQLException, IOException {
        Configuration configuration = new Configuration();
        populateConfiguration(configuration);

        // start invocation
        ShellCallback shellCallback = ShellCallbackFactory.createInstance(configuration.getContexts().get(0).getTargetRuntime());

        List<String> warnings = new ArrayList<>();
        Set<String> fullyQualifiedTables = new HashSet<>();
        Set<String> contexts = new HashSet<>();

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(configuration, shellCallback, warnings);
        myBatisGenerator.generate(progressCallback, contexts, fullyQualifiedTables);

        return warnings;
    }

    // discard export function since the library does not support anymore
//    public void export(File file) throws IOException {
//        Configuration configuration = new Configuration();
//        populateConfiguration(configuration);
//
//        // NOTE: hard code to fix the issue that url property contains '&';
//        JDBCConnectionConfiguration jdbcConfig = configuration.getContexts().get(0).getJdbcConnectionConfiguration();
//        String url = jdbcConfig.getConnectionURL().replace("&", "&amp;");
//        jdbcConfig.setConnectionURL(url);
//
//        String content = configuration.toDocument().getFormattedContent();
//        FileUtils.write(file, content, "UTF-8");
//    }

    private void populateConfiguration(Configuration configuration) {
        DefaultParameters defaultParameters = paramWrapper.getDefaultParameters();
        if (StringUtil.stringHasValue(paramWrapper.getDriverLibrary())) {
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

        // java type resolver, force big decimals
        JavaTypeResolverConfiguration javaTypeResolverConfig = new JavaTypeResolverConfiguration();
        javaTypeResolverConfig.addProperty(PropertyRegistry.TYPE_RESOLVER_FORCE_BIG_DECIMALS, defaultParameters.getForceBigDecimals().toString());
        javaTypeResolverConfig.addProperty(PropertyRegistry.TYPE_RESOLVER_USE_JSR310_TYPES, defaultParameters.getUseJSR310Types().toString());
        context.setJavaTypeResolverConfiguration(javaTypeResolverConfig);

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
            TableConfiguration tableConfig = paramWrapper.getDefaultTableConfigWrapper().createTableConfig(context);
            populateTableConfig(tableConfig, tableInfo);
            context.addTableConfiguration(tableConfig);
        }

        // comment config
        populateCommentConfig(context);

        // custom plugin
        populatePlugins(context);
    }

    private void populateCommentConfig(Context context) {
        CommentGeneratorConfiguration commentConfig = new CommentGeneratorConfiguration();
        commentConfig.setConfigurationType(CustomCommentGenerator.class.getName());
        commentConfig.addProperty(CustomCommentGenerator.ADD_DATABASE_REMARK, paramWrapper.getDatabaseRemark().toString());
        commentConfig.addProperty(CustomCommentGenerator.GENERATED_COMMENT, paramWrapper.getDefaultParameters().getGeneratedComment());
        context.setCommentGeneratorConfiguration(commentConfig);
    }

    private void populatePlugins(Context context) {
        {
            // support rename plugin by default
            PluginConfiguration pluginConfig = new PluginConfiguration();
            pluginConfig.setConfigurationType(RenamePlugin.class.getName());
            pluginConfig.addProperty("type", RenamePlugin.class.getName());
            populatePluginConfig(new PluginConfigWrapper(paramWrapper.getDefaultParameters().getRenameConfig()), pluginConfig);
            context.addPluginConfiguration(pluginConfig);
        }

        // add patch plugin for dynamic sql runtime
        if (DefaultParameters.MY_BATIS_3_DYNAMIC_SQL.equals(context.getTargetRuntime())) {
            context.addPluginConfiguration(createPluginConfig(DsqlRuntimePatchPlugin.class));

            // Disable Insert/Update/Delete
            TableConfigurationWrapper tableConfig = paramWrapper.getDefaultTableConfigWrapper();
            if (!tableConfig.isInsertStatementEnabled()) {
                context.addPluginConfiguration(createPluginConfig(DisableInsertPlugin.class));
            }
            if (!tableConfig.isUpdateByPrimaryKeyStatementEnabled()) {
                context.addPluginConfiguration(createPluginConfig(DisableUpdatePlugin.class));
            }
            if (!tableConfig.isDeleteByPrimaryKeyStatementEnabled()) {
                context.addPluginConfiguration(createPluginConfig(DisableDeletePlugin.class));
            }
        }

        if (paramWrapper.getSelectedPlugins().isEmpty()) {
            return;
        }

        for (Map.Entry<String, PluginConfigWrapper> entry : paramWrapper.getSelectedPlugins().entrySet()) {
            PluginConfiguration pluginConfig = new PluginConfiguration();
            pluginConfig.setConfigurationType(entry.getKey());
            pluginConfig.addProperty("type", entry.getKey());
            populatePluginConfig(entry.getValue(), pluginConfig);

            context.addPluginConfiguration(pluginConfig);
        }
    }

    @NotNull
    private PluginConfiguration createPluginConfig(Class pluginClass) {
        PluginConfiguration pluginConfig = new PluginConfiguration();
        pluginConfig.setConfigurationType(pluginClass.getName());
        pluginConfig.addProperty("type", pluginClass.getName());
        return pluginConfig;
    }

    private void populatePluginConfig(PluginConfigWrapper configWrapper, PluginConfiguration pluginConfig) {
        if (configWrapper != null) {
            Object config = configWrapper.getPluginConfig();
            for (Field field : config.getClass().getFields()) {
                PluginConfig annotation = field.getAnnotation(PluginConfig.class);
                if (Modifier.isStatic(field.getModifiers()) || annotation == null) {
                    continue;
                }

                try {
                    Object val = field.get(config);
                    if (val != null && StringUtil.stringHasValue(String.valueOf(val))) {
                        pluginConfig.addProperty(annotation.configKey(), String.valueOf(val));
                    } else if (StringUtil.stringHasValue(annotation.defaultValue())) {
                        pluginConfig.addProperty(annotation.configKey(), annotation.defaultValue());
                    }
                } catch (IllegalAccessException e) {
                    // NOOP
                }
            }
        }
    }

    private void populateTableConfig(TableConfiguration tableConfig, TableInfo tableInfo) {
        tableConfig.setTableName(tableInfo.getTableName());
        if (StringUtil.stringHasValue(tableInfo.getDomainName())) {
            tableConfig.setDomainObjectName(tableInfo.getDomainName());
        }

        GeneratedKeyWrapper generatedKeyWrapper = paramWrapper.getDefaultTableConfigWrapper().getGeneratedKeyWrapper();
        tableConfig.setGeneratedKey(generatedKeyWrapper.createGeneratedKey(tableInfo));

        // column setting
        if (tableInfo.getCustomColumns() != null) {
            for (ColumnInfo customColumn : tableInfo.getCustomColumns()) {
                if (ColumnActionEnum.OVERRIDE.equals(customColumn.getAction())) {
                    ColumnOverride columnOverride = new ColumnOverride(customColumn.getColumnName());
                    columnOverride.setJavaType(customColumn.getJavaType());
                    columnOverride.setJavaProperty(customColumn.getJavaProperty());
                    tableConfig.addColumnOverride(columnOverride);
                } else if (ColumnActionEnum.IGNORE.equals(customColumn.getAction())) {
                    tableConfig.addIgnoredColumn(new IgnoredColumn(customColumn.getColumnName()));
                }
            }
        }
    }

    public static List<String> runWithConfigurationFile(String path, Properties properties, ProgressCallback processCallback)
            throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        // to avoid xml parsing issue
        String origin = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        try {
            List<String> warnings = new ArrayList<>();
            ConfigurationParser parser = new ConfigurationParser(warnings);
            Configuration configuration = parser.parseConfiguration(new File(path));
            validateClassPath(configuration.getClassPathEntries(), properties);

            Context context = configuration.getContexts().get(0);
            context.getProperties().putAll(properties);
            validateTargetProject(context);

            ShellCallback shellCallback = ShellCallbackFactory.createInstance(context.getTargetRuntime());
            MyBatisGenerator generator = new MyBatisGenerator(configuration, shellCallback, warnings);
            generator.generate(processCallback);
            return warnings;
        } finally {
            // restore
            if (origin != null) {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory", origin);
            }
        }

    }

    private static void validateClassPath(List<String> classPathEntries, Properties properties) throws IOException {
        if (classPathEntries != null) {
            List<String> resolved = new ArrayList<>();
            for (String classPath : classPathEntries) {
                String resolvedPath = resolve(classPath, properties);
                if (!new File(resolvedPath).exists()) {
                    throw new FileNotFoundException("Class path not found: " + resolvedPath);
                }
                resolved.add(resolvedPath);
            }
            classPathEntries.clear();
            classPathEntries.addAll(resolved);
        }
    }

    private static void validateTargetProject(Context context) throws IOException {
        JavaClientGeneratorConfiguration javaClientConfig = context.getJavaClientGeneratorConfiguration();
        javaClientConfig.setTargetProject(resolve(javaClientConfig.getTargetProject(), context.getProperties()));
        File file = new File(javaClientConfig.getTargetProject());
        if (!file.exists()) {
            throw new FileNotFoundException("Target project not found: " + file.getCanonicalPath());
        }

        JavaModelGeneratorConfiguration javaModelConfig = context.getJavaModelGeneratorConfiguration();
        javaModelConfig.setTargetProject(resolve(javaModelConfig.getTargetProject(), context.getProperties()));
        file = new File(javaModelConfig.getTargetProject());
        if (!file.exists()) {
            throw new FileNotFoundException("Target project not found: " + file.getCanonicalPath());
        }

        if (!"ANNOTATEDMAPPER".equalsIgnoreCase(javaClientConfig.getConfigurationType())) {
            SqlMapGeneratorConfiguration sqlMapConfig = context.getSqlMapGeneratorConfiguration();
            sqlMapConfig.setTargetProject(resolve(sqlMapConfig.getTargetProject(), context.getProperties()));
            file = new File(sqlMapConfig.getTargetProject());
            if (!file.exists()) {
                throw new FileNotFoundException("Target project not found: " + file.getCanonicalPath());
            }
        }
    }

    private static String resolve(String txt, Properties properties) {
        String resolved = txt;
        if (resolved.contains("${PROJECT_DIR}")) {
            resolved = resolved.replace("${PROJECT_DIR}", properties.getProperty("PROJECT_DIR"));
        }
        if (resolved.contains("${CURRENT_DIR}")) {
            resolved = resolved.replace("${CURRENT_DIR}", properties.getProperty("CURRENT_DIR"));
        }
        return resolved;
    }
}