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
            // built-in driver uses its enum url pattern; a registered driver
            // (driverType == null) carries its template in urlPattern
            String url = connectionInfo.getDriverType() != null
                    ? connectionInfo.getDriverType().getUrlPattern()
                    : connectionInfo.getUrlPattern();
            if (url == null) {
                return "";
            }
            url = url.replace("${host}", emptyIfNull(connectionInfo.getHost()));
            url = url.replace("${port}", String.valueOf(connectionInfo.getPort()));
            url = url.replace("${db}", emptyIfNull(connectionInfo.getDatabase()));
            return url;
        }
    }

    private static String emptyIfNull(String s) {
        return s == null ? "" : s;
    }
}

