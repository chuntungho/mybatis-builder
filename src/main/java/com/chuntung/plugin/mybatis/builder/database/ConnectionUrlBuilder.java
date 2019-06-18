/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.database;

import com.chuntung.plugin.mybatis.builder.model.ConnectionInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;

public class ConnectionUrlBuilder {
    private ConnectionInfo connectionInfo;

    public ConnectionUrlBuilder(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public String getConnectionUrl() {
        if (StringUtil.stringHasValue(connectionInfo.getUrl())) {
            return connectionInfo.getUrl();
        } else {
            String url = connectionInfo.getDriverType().getUrlPattern();
            url = url.replace("${host}", connectionInfo.getHost());
            url = url.replace("${port}", String.valueOf(connectionInfo.getPort()));
            url = url.replace("${db}", connectionInfo.getDatabase());
            return url;
        }
    }
}

