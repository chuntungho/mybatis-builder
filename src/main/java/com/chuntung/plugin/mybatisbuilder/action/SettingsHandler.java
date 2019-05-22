/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.action;

import com.chuntung.plugin.mybatisbuilder.MybatisBuilderService;
import com.chuntung.plugin.mybatisbuilder.generator.DefaultParameters;
import com.chuntung.plugin.mybatisbuilder.model.ConnectionInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.sql.SQLException;
import java.util.List;

public class SettingsHandler {
    private Project project;
    private final MybatisBuilderService service;

    public static SettingsHandler getInstance(Project project) {
        return new SettingsHandler(project);
    }

    public SettingsHandler(Project project) {
        this.project = project;
        this.service = MybatisBuilderService.getInstance(project);
    }

    public List<ConnectionInfo> loadConnectionInfoList() {
        return service.loadConnectionInfoListWithPassword();
    }

    public DefaultParameters getDefaultParameters() {
        return service.getDefaultParameters();
    }

    public void testConnection(ConnectionInfo connectionInfo) {
        try {
            service.testConnection(connectionInfo);
            Messages.showInfoMessage("Connection to [" + connectionInfo.getName() + "] was successful", "Connection Successful");
        } catch (SQLException e) {
            Messages.showErrorDialog(e.getMessage(), "Connection Error");
        }
    }

    public void saveAll(List<ConnectionInfo> list, DefaultParameters defaultParameters) {
        service.saveConnectionInfo(list);
        service.saveDefaultParameters(defaultParameters);
    }

}