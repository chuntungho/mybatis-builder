/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.CustomDriverInfo;
import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.chuntung.plugin.mybatis.builder.MybatisBuilderTopics;
import com.chuntung.plugin.mybatis.builder.generator.DefaultParameters;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.sql.SQLException;
import java.util.List;

public class SettingsPresenter {
    private final Project project;
    private final MybatisBuilderService service;

    public static SettingsPresenter getInstance(Project project) {
        return new SettingsPresenter(project);
    }

    public SettingsPresenter(Project project) {
        this.project = project;
        this.service = MybatisBuilderService.getInstance(project);
    }

    public List<ConnectionInfo> loadConnectionInfoList() {
        return service.loadConnectionInfoListWithPassword();
    }

    public DefaultParameters getDefaultParameters() {
        return service.getDefaultParameters();
    }

    public List<CustomDriverInfo> loadCustomDrivers() {
        return service.loadCustomDrivers();
    }

    public void saveCustomDrivers(List<CustomDriverInfo> customDrivers) {
        service.saveCustomDrivers(customDrivers);
    }

    public void testConnection(ConnectionInfo connectionInfo) {
        try {
            service.testConnection(connectionInfo);
            Messages.showInfoMessage("Connection to [" + connectionInfo.getName() + "] was successful", "Connection Successful");
        } catch (SQLException e) {
            String message = e.getMessage();
            if (e.getCause() instanceof java.net.UnknownHostException) {
                message = "Unknown host: " + e.getCause().getMessage();
            }
            Messages.showErrorDialog(message, "Connection Error");
        }
    }

    public void saveAll(List<ConnectionInfo> list, DefaultParameters defaultParameters) {
        service.saveConnectionInfo(list);
        service.saveDefaultParameters(defaultParameters);
        project.getMessageBus()
                .syncPublisher(MybatisBuilderTopics.CONNECTIONS_CHANGED)
                .connectionsChanged();
    }

    public void clearHistory() {
        service.clearHistory();
    }
}