/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.generator.callback.IndicatorProcessCallback;
import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.DatabaseItem;
import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.chuntung.plugin.mybatis.builder.database.ConnectionUrlBuilder;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorToolWrapper;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.chuntung.plugin.mybatis.builder.view.MybatisBuilderParametersDialog;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The key building controller.
 *
 * @author Tony Ho
 */
public class BuildAction extends DumbAwareAction {
    private static final Logger logger = LoggerFactory.getLogger(BuildAction.class);

    // NOTE: action id should be consistent with plugin.xml
    private static final String ACTION_ID = "MyBatisBuilder.Build";

    private static final Key<TreeSelectionModel> SELECTION_MODEL_KEY = new Key<>(ACTION_ID);

    public static AnAction getInstance(Project project) {
        return ActionManager.getInstance().getAction(ACTION_ID);
    }

    // put selection model in project scope for build action
    public static void registerSelectionModel(Project project, TreeSelectionModel model) {
        project.putUserData(SELECTION_MODEL_KEY, model);
    }

    private static TreeSelectionModel getSelectionModel(Project project) {
        return project.getUserData(SELECTION_MODEL_KEY);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MybatisBuilderService service = MybatisBuilderService.getInstance(project);
        TreeSelectionModel selectionModel = BuildAction.getSelectionModel(project);

        // load the last parameters
        GeneratorParamWrapper paramWrapper = service.getLastGeneratorParamWrapper();

        // load default parameters
        paramWrapper.setDefaultParameters(service.getDefaultParameters());

        // enable sub packages by default
        enableSubPackages(paramWrapper.getJavaModelConfig(), paramWrapper.getJavaClientConfig(), paramWrapper.getSqlMapConfig());

        // populate selected tables
        ConnectionInfo connectionInfo = new ConnectionInfo();
        String msg = populateSelectedTables(service, paramWrapper, connectionInfo, selectionModel);
        if (msg != null) {
            Messages.showWarningDialog(msg, "Building Failed");
            return;
        }

        // get connection by id, specify database, then test connection
        try {
            ConnectionInfo savedConnectionIfo = service.getConnectionInfoWithPassword(connectionInfo.getId());
            savedConnectionIfo.setDatabase(connectionInfo.getDatabase());
            service.testConnection(savedConnectionIfo);

            populateConnection(paramWrapper, savedConnectionIfo);
        } catch (SQLException e) {
            logger.warn("Failed to connect to database", e);
            Messages.showErrorDialog(e.getMessage(), "Building Error");
            return;
        }

        // populate project and package
        populateProjectAndPackage(paramWrapper, project);

        MybatisBuilderParametersDialog dialog = new MybatisBuilderParametersDialog(project, paramWrapper, connectionInfo.getId());

        if (dialog.showAndGet()) {
            // stash last parameters
            service.stashGeneratorParamWrapper(paramWrapper);

            new Task.Backgroundable(project, "MyBatis Builder") {

                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    IndicatorProcessCallback processCallback = new IndicatorProcessCallback(progressIndicator);
                    GeneratorToolWrapper toolWrapper = new GeneratorToolWrapper(paramWrapper, processCallback);
                    try {
                        List<String> warnings = toolWrapper.generate();

                        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
                        if (projectDir != null) {
                            VfsUtil.markDirtyAndRefresh(true, true, true, projectDir);
                        }

                        int cnt = paramWrapper.getSelectedTables().size();
                        String msg = "Generation for " + cnt + (cnt > 1 ? " tables" : " table") + " was done.";
                        if (!warnings.isEmpty()) {
                            msg = msg + "\n" + String.join("\n", warnings);
                        }
                        NotificationHelper.getInstance().notifyInfo(msg, project);
                    } catch (Exception e) {
                        logger.warn("Failed to generate", e);
                        NotificationHelper.getInstance().notifyError(String.valueOf(e.getMessage()), project);
                    }
                }
            }.queue();
        }
    }

    private String populateSelectedTables(MybatisBuilderService service, GeneratorParamWrapper paramWrapper,
                                          ConnectionInfo info, TreeSelectionModel selectionModel) {
        String msg = null;
        if (selectionModel == null) {
            msg = "Please open tool window first";
            return msg;
        }

        List<TableInfo> tables = new ArrayList<>();
        TreePath[] selectionPaths = selectionModel.getSelectionPaths();
        if (selectionPaths != null && selectionPaths.length > 0) {
            for (TreePath treePath : selectionPaths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
                DatabaseItem item = (DatabaseItem) node.getUserObject();
                if (DatabaseItem.ItemTypeEnum.TABLE.equals(item.getType())) {
                    // only support tables in the same database
                    DefaultMutableTreeNode databaseNode = (DefaultMutableTreeNode) node.getParent();
                    String database = databaseNode.getUserObject().toString();
                    if (!database.equals(info.getDatabase())) {
                        if (info.getDatabase() == null) {
                            info.setDatabase(database);
                        } else {
                            msg = "Only support the tables in the same database";
                            break;
                        }
                    }

                    // only support tables in the same connection
                    DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) databaseNode.getParent();
                    String connectionId = ((DatabaseItem) connectionNode.getUserObject()).getId();
                    if (!connectionId.equals(info.getId())) {
                        if (info.getId() == null) {
                            info.setId(connectionId);
                        } else {
                            msg = "Only support the tables in the same connection";
                            break;
                        }
                    }

                    TableInfo tableInfo = new TableInfo(info.getDatabase(), item.getName());
                    TableInfo lastTableInfo = service.getLastTableInfo(tableInfo);
                    if (lastTableInfo != null) {
                        XmlSerializerUtil.copyBean(lastTableInfo, tableInfo);
                        // fix v1.0.1 upgrade issue: reset since old version use a different name
                        tableInfo.setDatabase(database);
                    }

                    // pre-gen domain name for reference
                    if (StringUtil.isBlank(tableInfo.getDomainName())) {
                        String domainName = JavaBeansUtil.getCamelCaseString(tableInfo.getTableName(), true);
                        tableInfo.setDomainName(domainName);
                    }

                    tables.add(tableInfo);
                }
            }
        }

        paramWrapper.setSelectedTables(tables);

        if (tables.size() == 0) {
            msg = "There is no table selected";
        }

        return msg;
    }

    private void populateConnection(GeneratorParamWrapper paramWrapper, ConnectionInfo connectionInfo) {
        // dynamic library
        if (StringUtil.stringHasValue(connectionInfo.getDriverLibrary())) {
            paramWrapper.setDriverLibrary(connectionInfo.getDriverLibrary());
        }

        JDBCConnectionConfiguration jdbcConfig = paramWrapper.getJdbcConfig();
        // the known driver class or custom driver class
        String driverClass = StringUtil.stringHasValue(connectionInfo.getDriverClass()) ?
                connectionInfo.getDriverClass() : connectionInfo.getDriverType().getDriverClass();
        jdbcConfig.setDriverClass(driverClass);

        // connection url, should contain database
        String connectionUrl = new ConnectionUrlBuilder(connectionInfo).getConnectionUrl();
        jdbcConfig.setConnectionURL(connectionUrl);

        jdbcConfig.setUserId(connectionInfo.getUserName());
        jdbcConfig.setPassword(connectionInfo.getPassword());
    }

    private void enableSubPackages(PropertyHolder... holders) {
        for (PropertyHolder holder : holders) {
            holder.addProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES, "true");
        }
    }

    private void populateProjectAndPackage(GeneratorParamWrapper paramWrapper, Project project) {
        // maven/gradle default path
        String projectPath = project.getBasePath();
        String sourceCodeRoot = projectPath + "/src/main/java";
        String resourcesRoot = projectPath + "/src/main/resources";
        if (!new File(sourceCodeRoot).exists()) {
            sourceCodeRoot = projectPath;
        }
        if (!new File(resourcesRoot).exists()) {
            resourcesRoot = projectPath;
        }

        // initialize project paths
        if (paramWrapper.getJavaModelConfig().getTargetProject() == null) {
            paramWrapper.getJavaModelConfig().setTargetProject(sourceCodeRoot);
            paramWrapper.getJavaModelConfig().setTargetPackage("model");
        }

        if (paramWrapper.getJavaClientConfig().getTargetProject() == null) {
            paramWrapper.getJavaClientConfig().setTargetProject(sourceCodeRoot);
            paramWrapper.getJavaClientConfig().setTargetPackage("mapper");
        }

        if (paramWrapper.getSqlMapConfig().getTargetProject() == null) {
            paramWrapper.getSqlMapConfig().setTargetProject(resourcesRoot);
            paramWrapper.getSqlMapConfig().setTargetPackage("sqlmap");
        }
    }
}
