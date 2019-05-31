/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.action.idea;

import com.chuntung.plugin.mybatisbuilder.MybatisBuilderService;
import com.chuntung.plugin.mybatisbuilder.database.ConnectionUrlBuilder;
import com.chuntung.plugin.mybatisbuilder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatisbuilder.generator.GeneratorToolWrapper;
import com.chuntung.plugin.mybatisbuilder.generator.TableInfo;
import com.chuntung.plugin.mybatisbuilder.model.ConnectionInfo;
import com.chuntung.plugin.mybatisbuilder.model.DatabaseItem;
import com.chuntung.plugin.mybatisbuilder.view.MybatisBuilderParametersDialog;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mybatis.generator.config.JDBCConnectionConfiguration;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The key building controller.
 *
 * @author Tony Ho
 */
public class BuildAction extends AnAction {
    private static final Logger logger = LoggerFactory.getLogger(BuildAction.class);
    private JTree objectTree;
    private NotificationGroup notificationGroup;

    public BuildAction(JTree objectTree) {
        super("Build...", null, IconLoader.getIcon("/images/build.png"));
        this.objectTree = objectTree;
        this.notificationGroup = new NotificationGroup(
                "MybatisBuilder.NotificationGroup",
                NotificationDisplayType.BALLOON, true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        MybatisBuilderService service = MybatisBuilderService.getInstance(project);

        // load the last parameters
        GeneratorParamWrapper paramWrapper = service.getLastGeneratorParamWrapper();

        // load default parameters
        paramWrapper.setDefaultParameters(service.getDefaultParameters());

        // enable sub packages by default
        enableSubPackages(paramWrapper.getJavaModelConfig(), paramWrapper.getJavaClientConfig(), paramWrapper.getSqlMapConfig());

        // populate selected tables
        ConnectionInfo connectionInfo = new ConnectionInfo();
        String msg = populateSelectedTables(service, paramWrapper, connectionInfo);
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
        }

        // populate project and package
        populateProjectAndPackage(paramWrapper, project);

        MybatisBuilderParametersDialog dialog = new MybatisBuilderParametersDialog(project, paramWrapper);

        if (dialog.showAndGet()) {
            // stash last parameters
            service.stashGeneratorParamWrapper(paramWrapper);

            GeneratorToolWrapper toolWrapper = new GeneratorToolWrapper(paramWrapper);
            try {
                toolWrapper.generate();

                // NOTE: syncRefresh run in background will raise Write-unsafe exception
                // therefore not to use SwingUtilities.invokeLater
                VirtualFileManager.getInstance().syncRefresh();

                int cnt = paramWrapper.getSelectedTables().size();
                String successMsg = cnt + (cnt > 1 ? " tables were built" : " table was built") + ", sync project folder for details";
                Notification success = notificationGroup.createNotification(successMsg, NotificationType.INFORMATION);
                Notifications.Bus.notify(success, project);
            } catch (Exception e) {
                logger.error("Failed to generate", e);
                Messages.showErrorDialog(e.getMessage(), "Building Error");
            }
        }
    }

    private String populateSelectedTables(MybatisBuilderService service, GeneratorParamWrapper paramWrapper, ConnectionInfo info) {
        List<TableInfo> tables = new ArrayList<>();

        String msg = null;
        TreePath[] selectionPaths = objectTree.getSelectionPaths();
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
                    if (StringUtils.isBlank(tableInfo.getDomainName())) {
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
        if (StringUtils.isNotBlank(connectionInfo.getDriverLibrary())) {
            paramWrapper.setDriverLibrary(connectionInfo.getDriverLibrary());
        }

        JDBCConnectionConfiguration jdbcConfig = paramWrapper.getJdbcConfig();
        // the known driver class or custom driver class
        String driverClass = StringUtils.isNotBlank(connectionInfo.getDriverClass()) ?
                connectionInfo.getDriverClass() : connectionInfo.getDriverType().getDriverClass();
        jdbcConfig.setDriverClass(driverClass);

        // connection url, should contains database
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
