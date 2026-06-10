/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator;

import com.chuntung.plugin.mybatis.builder.generator.annotation.PluginConfig;
import com.chuntung.plugin.mybatis.builder.generator.plugins.RenamePlugin;
import com.chuntung.plugin.mybatis.builder.model.ColumnActionEnum;
import com.chuntung.plugin.mybatis.builder.model.ColumnInfo;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.*;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;
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
        // to avoid xml parsing issue in MBG 1.4.1-SNAPSHOT
        String origin = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        try {
            Configuration configuration = new Configuration();
            populateConfiguration(configuration);

            return new MyBatisGenerator.Builder()
                    .withConfiguration(configuration)
                    .withShellCallback(new DefaultShellCallback())
                    .withProgressCallback(progressCallback)
                    .withOverwriteEnabled(true)
                    // merge existing files: MBG's JavaParser merger preserves custom members
                    // (those without the @Generated annotation) and regenerates the rest
                    .withJavaFileMergeEnabled(true)
                    .build()
                    .generateAndWrite();
        } finally {
            if (origin != null) {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory", origin);
            }
        }
    }

    private void populateConfiguration(Configuration configuration) {
        DefaultParameters defaultParameters = paramWrapper.getDefaultParameters();
        if (StringUtil.stringHasValue(paramWrapper.getDriverLibrary())) {
            configuration.addClasspathEntry(paramWrapper.getDriverLibrary());
        }

        JavaTypeResolverConfiguration javaTypeResolverConfig = new JavaTypeResolverConfiguration.Builder()
                .withProperty(new Property(PropertyRegistry.TYPE_RESOLVER_FORCE_BIG_DECIMALS, defaultParameters.getForceBigDecimals().toString()))
                .build();

        Context.Builder contextBuilder = new Context.Builder()
                .withId("mybatis-builder")
                .withDefaultModelType(defaultParameters.getDefaultModelType())
                .withTargetRuntime(paramWrapper.getTargetRuntime())
                .withProperty(new Property(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING, defaultParameters.getJavaFileEncoding()))
                .withProperty(new Property(PropertyRegistry.CONTEXT_AUTO_DELIMIT_KEYWORDS, "true"))
                .withProperty(new Property(PropertyRegistry.CONTEXT_BEGINNING_DELIMITER, paramWrapper.getBeginningDelimiter()))
                .withProperty(new Property(PropertyRegistry.CONTEXT_ENDING_DELIMITER, paramWrapper.getEndingDelimiter()))
                .withJavaTypeResolverConfiguration(javaTypeResolverConfig)
                .withJdbcConnectionConfiguration(buildJdbcConfig())
                .withModelGeneratorConfiguration(buildModelConfig())
                .withClientGeneratorConfiguration(buildClientConfig())
                .withSqlMapGeneratorConfiguration(buildSqlMapConfig())
                .withCommentGeneratorConfiguration(buildCommentConfig());

        populatePlugins(contextBuilder);

        for (TableInfo tableInfo : paramWrapper.getSelectedTables()) {
            contextBuilder.withTableConfiguration(buildTableConfig(tableInfo));
        }

        configuration.addContext(contextBuilder.build());
    }

    private JDBCConnectionConfiguration buildJdbcConfig() {
        JdbcConnectionConfig src = paramWrapper.getJdbcConfig();
        JDBCConnectionConfiguration.Builder builder = new JDBCConnectionConfiguration.Builder()
                .withDriverClass(src.getDriverClass())
                .withConnectionURL(src.getConnectionURL())
                .withProperties(src.getProperties());
        if (src.getUserId() != null) builder.withUserId(src.getUserId());
        if (src.getPassword() != null) builder.withPassword(src.getPassword());
        return builder.build();
    }

    private ModelGeneratorConfiguration buildModelConfig() {
        JavaModelGeneratorConfig src = paramWrapper.getJavaModelConfig();
        ModelGeneratorConfiguration.Builder builder = new ModelGeneratorConfiguration.Builder()
                .withTargetPackage(src.getTargetPackage())
                .withTargetProject(src.getTargetProject())
                .withProperties(src.getProperties());
        if (Boolean.TRUE.equals(paramWrapper.getTrimStrings())) {
            builder.withProperty(new Property(PropertyRegistry.MODEL_GENERATOR_TRIM_STRINGS, "true"));
        }
        return builder.build();
    }

    private ClientGeneratorConfiguration buildClientConfig() {
        JavaClientGeneratorConfig src = paramWrapper.getJavaClientConfig();
        ClientGeneratorConfiguration.Builder builder = new ClientGeneratorConfiguration.Builder()
                .withTargetPackage(src.getTargetPackage())
                .withTargetProject(src.getTargetProject())
                .withProperties(src.getProperties());
        ClientGeneratorConfiguration.LegacyClientType legacyType = mapLegacyClientType(src.getConfigurationType());
        if (legacyType != null) {
            builder.withLegacyClientType(legacyType);
        }
        return builder.build();
    }

    private static ClientGeneratorConfiguration.LegacyClientType mapLegacyClientType(String type) {
        if (type == null) return null;
        switch (type.toUpperCase(Locale.ROOT)) {
            case "XMLMAPPER": return ClientGeneratorConfiguration.LegacyClientType.XML_MAPPER;
            case "ANNOTATEDMAPPER": return ClientGeneratorConfiguration.LegacyClientType.ANNOTATED_MAPPER;
            case "MIXEDMAPPER": return ClientGeneratorConfiguration.LegacyClientType.MIXED_MAPPER;
            default: return null;
        }
    }

    private SqlMapGeneratorConfiguration buildSqlMapConfig() {
        SqlMapGeneratorConfig src = paramWrapper.getSqlMapConfig();
        return new SqlMapGeneratorConfiguration.Builder()
                .withTargetPackage(src.getTargetPackage())
                .withTargetProject(src.getTargetProject())
                .withProperties(src.getProperties())
                .build();
    }

    private CommentGeneratorConfiguration buildCommentConfig() {
        CommentGeneratorConfiguration.Builder builder = new CommentGeneratorConfiguration.Builder()
                .withConfigurationType(CustomCommentGenerator.class.getName())
                .withProperty(new Property(CustomCommentGenerator.ADD_DATABASE_REMARK, paramWrapper.getDatabaseRemark().toString()));
        String generatedComment = paramWrapper.getDefaultParameters().getGeneratedComment();
        if (generatedComment != null) {
            builder.withProperty(new Property(CustomCommentGenerator.GENERATED_COMMENT, generatedComment));
        }
        return builder.build();
    }

    private void populatePlugins(Context.Builder contextBuilder) {
        // RenamePlugin is always present
        contextBuilder.withPluginConfiguration(
                createPluginConfig(RenamePlugin.class.getName(),
                        new PluginConfigWrapper(paramWrapper.getDefaultParameters().getRenameConfig())));

        // DSQL patches
        if (GeneratorParamWrapper.MY_BATIS_3_DYNAMIC_SQL.equals(paramWrapper.getTargetRuntime())) {
            TableConfigurationWrapper tableConfig = paramWrapper.getDefaultTableConfigWrapper();
            if (!tableConfig.isInsertStatementEnabled()) {
                contextBuilder.withPluginConfiguration(createPluginConfig(DisableInsertPlugin.class.getName(), null));
            }
            if (!tableConfig.isUpdateByPrimaryKeyStatementEnabled()) {
                contextBuilder.withPluginConfiguration(createPluginConfig(DisableUpdatePlugin.class.getName(), null));
            }
            if (!tableConfig.isDeleteByPrimaryKeyStatementEnabled()) {
                contextBuilder.withPluginConfiguration(createPluginConfig(DisableDeletePlugin.class.getName(), null));
            }
        }

        for (Map.Entry<String, PluginConfigWrapper> entry : paramWrapper.getSelectedPlugins().entrySet()) {
            contextBuilder.withPluginConfiguration(createPluginConfig(entry.getKey(), entry.getValue()));
        }
    }

    @NotNull
    private PluginConfiguration createPluginConfig(String pluginClass, PluginConfigWrapper configWrapper) {
        PluginConfiguration.Builder builder = new PluginConfiguration.Builder()
                .withConfigurationType(pluginClass)
                .withProperty(new Property("type", pluginClass));
        if (configWrapper != null) {
            populatePluginConfig(configWrapper, builder);
        }
        return builder.build();
    }

    private void populatePluginConfig(PluginConfigWrapper configWrapper, PluginConfiguration.Builder builder) {
        if (configWrapper == null) return;
        Object config = configWrapper.getPluginConfig();
        for (Field field : config.getClass().getFields()) {
            PluginConfig annotation = field.getAnnotation(PluginConfig.class);
            if (Modifier.isStatic(field.getModifiers()) || annotation == null) {
                continue;
            }
            try {
                Object val = field.get(config);
                if (val != null && StringUtil.stringHasValue(String.valueOf(val))) {
                    builder.withProperty(new Property(annotation.configKey(), String.valueOf(val)));
                } else if (StringUtil.stringHasValue(annotation.defaultValue())) {
                    builder.withProperty(new Property(annotation.configKey(), annotation.defaultValue()));
                }
            } catch (IllegalAccessException e) {
                // NOOP
            }
        }
    }

    private TableConfiguration buildTableConfig(TableInfo tableInfo) {
        TableConfiguration.Builder builder = paramWrapper.getDefaultTableConfigWrapper().createTableConfigBuilder();

        builder.withTableName(tableInfo.getTableName());
        if (StringUtil.stringHasValue(tableInfo.getDomainName())) {
            builder.withDomainObjectName(tableInfo.getDomainName());
        }

        GeneratedKeyWrapper generatedKeyWrapper = paramWrapper.getDefaultTableConfigWrapper().getGeneratedKeyWrapper();
        GeneratedKey generatedKey = generatedKeyWrapper.createGeneratedKey(tableInfo);
        if (generatedKey != null) {
            builder.withGeneratedKey(generatedKey);
        }

        if (tableInfo.getCustomColumns() != null) {
            for (ColumnInfo customColumn : tableInfo.getCustomColumns()) {
                if (ColumnActionEnum.OVERRIDE.equals(customColumn.getAction())) {
                    ColumnOverride.Builder coBuilder = new ColumnOverride.Builder()
                            .withColumnName(customColumn.getColumnName());
                    if (StringUtil.stringHasValue(customColumn.getJavaType())) {
                        coBuilder.withJavaType(customColumn.getJavaType());
                    }
                    if (StringUtil.stringHasValue(customColumn.getJavaProperty())) {
                        coBuilder.withJavaProperty(customColumn.getJavaProperty());
                    }
                    builder.withColumnOverride(coBuilder.build());
                } else if (ColumnActionEnum.IGNORE.equals(customColumn.getAction())) {
                    builder.withIgnoredColumn(new IgnoredColumn(customColumn.getColumnName(), false));
                }
            }
        }

        return builder.build();
    }

    public static List<String> runWithConfigurationFile(String path, Properties properties, ProgressCallback processCallback)
            throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        // to avoid xml parsing issue in MBG 1.4.1-SNAPSHOT
        String origin = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        try {
            // Pass properties so ConfigurationParser substitutes ${...} tokens during parse
            ConfigurationParser parser = new ConfigurationParser(properties);
            Configuration configuration = parser.parseConfiguration(new File(path));
            validateClassPath(configuration.getClassPathEntries(), properties);

            Context context = configuration.getContexts().get(0);
            validateTargetProject(context);

            List<String> warnings = new MyBatisGenerator.Builder()
                    .withConfiguration(configuration)
                    .withShellCallback(new DefaultShellCallback())
                    .withProgressCallback(processCallback)
                    .withOverwriteEnabled(true)
                    .withJavaFileMergeEnabled(true)
                    .build()
                    .generateAndWrite();

            List<String> allWarnings = new ArrayList<>(warnings);
            allWarnings.addAll(parser.getWarnings());
            return allWarnings;
        } finally {
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
        ModelGeneratorConfiguration modelConfig = context.getModelGeneratorConfiguration();
        if (!new File(modelConfig.getTargetProject()).exists()) {
            throw new FileNotFoundException("Target project not found: " + new File(modelConfig.getTargetProject()).getCanonicalPath());
        }

        Optional<ClientGeneratorConfiguration> clientConfigOpt = context.getClientGeneratorConfiguration();
        if (clientConfigOpt.isPresent()) {
            ClientGeneratorConfiguration clientConfig = clientConfigOpt.get();
            if (!new File(clientConfig.getTargetProject()).exists()) {
                throw new FileNotFoundException("Target project not found: " + new File(clientConfig.getTargetProject()).getCanonicalPath());
            }

            if (clientConfig.requiresXmlMapper()) {
                Optional<SqlMapGeneratorConfiguration> sqlMapConfigOpt = context.getSqlMapGeneratorConfiguration();
                if (sqlMapConfigOpt.isPresent()) {
                    String sqlMapProject = sqlMapConfigOpt.get().getTargetProject();
                    if (!new File(sqlMapProject).exists()) {
                        throw new FileNotFoundException("Target project not found: " + new File(sqlMapProject).getCanonicalPath());
                    }
                }
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
