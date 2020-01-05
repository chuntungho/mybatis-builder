/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.model.MybatisBuilderSettings;
import com.chuntung.plugin.mybatis.builder.model.TableInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The unified repository.
 *
 * @author Tony Ho
 */
@State(
        name = MybatisBuilderSettingsManager.STATE_NAME,
        storages = @Storage(MybatisBuilderSettingsManager.STORAGE_FILE)
)
public class MybatisBuilderSettingsManager implements ProjectComponent, PersistentStateComponent<MybatisBuilderSettings> {
    static final String STATE_NAME = "MybatisBuilder.project.settings";
    static final String STORAGE_FILE = "mybatisbuilder.xml";

    private MybatisBuilderSettings settings = new MybatisBuilderSettings();

    public static MybatisBuilderSettingsManager getInstance(Project project) {
        return ServiceManager.getService(project, MybatisBuilderSettingsManager.class);
    }

    public MybatisBuilderSettings getSettings() {
        return settings;
    }

    public void saveConnectionInfo(List<ConnectionInfo> connectionInfoList) {
        for (ConnectionInfo connection : connectionInfoList) {
            CredentialAttributes attr = getCredentialAttr(connection);
            Credentials credentials = new Credentials(attr.getUserName(), connection.getPassword());
            PasswordSafe.getInstance().set(attr, credentials);
        }

        settings.setConnectionInfoList(connectionInfoList);
    }

    public String getConnectionPassword(ConnectionInfo dto) {
        CredentialAttributes attr = getCredentialAttr(dto);
        String password = PasswordSafe.getInstance().getPassword(attr);
        return password;
    }

    private CredentialAttributes getCredentialAttr(ConnectionInfo connection) {
        String serviceName = String.format("MybatisBuilderConnection_%s", connection.getId());
        CredentialAttributes attributes = new CredentialAttributes(serviceName, connection.getUserName(), this.getClass(), false);
        return attributes;
    }

    public void saveTableInfo(List<TableInfo> tableInfoList) {
        for (TableInfo tableInfo : tableInfoList) {
            String key = formatMappingKey(tableInfo);
            settings.getTableInfoMap().put(key, tableInfo);
        }
    }

    public TableInfo getTableInfo(TableInfo param) {
        String key = formatMappingKey(param);
        return settings.getTableInfoMap().get(key);
    }

    @NotNull
    private String formatMappingKey(TableInfo tableInfo) {
        return tableInfo.getDatabase() + "#" + tableInfo.getTableName();
    }

    @Nullable
    @Override
    public MybatisBuilderSettings getState() {
        return settings;
    }

    @Override
    public void loadState(@NotNull MybatisBuilderSettings settings) {
        XmlSerializerUtil.copyBean(settings, this.settings);
    }

    public void clearHistory() {
        settings.getHistoryMap().clear();
    }

    public void addHistory(String category, String value) {
        if (StringUtil.isBlank(value)) {
            return;
        }

        List<String> histories = settings.getHistoryMap().get(category);
        if (histories == null) {
            histories = new ArrayList<>();
            settings.getHistoryMap().put(category, histories);
        }

        // insert at first
        histories.remove(value);
        histories.add(0, value);
        resize(histories, settings.getDefaultParameters().getHistorySize());
    }

    private void resize(List<?> list, int fixedSize) {
        while (list.size() > fixedSize) {
            list.remove(list.size() - 1);
        }
    }
}
