/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.model.PluginInfo;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.util.Url;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AboutDialog extends DialogWrapper {
    private JPanel contentPanel;
    private JLabel authorLabel;
    private JLabel pluginLabel;
    private JLabel homeLabel;
    private JLabel paypalLabel;
    private JLabel alipayLabel;

    class UrlRunnable implements Runnable {
        private String url;

        UrlRunnable(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            BrowserUtil.browse(url);
        }
    }

    public AboutDialog(Project project) {
        super(project, false);
        setOKButtonText("Close");
        setTitle("MyBatis Builder - About");
        setButtonsAlignment(SwingConstants.CENTER);

        Cursor hand = new Cursor(Cursor.HAND_CURSOR);

        String pluginVersion = PluginManager.getPlugin(PluginId.getId(PluginInfo.PLUGIN_ID)).getVersion();
        pluginLabel.setText(PluginInfo.PLUGIN_NAME + " v" + pluginVersion);

        init();
    }

    // just show OK button
    public Action[] createActions() {
        return new Action[]{getOKAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPanel;
    }

    private void createUIComponents() {
        authorLabel = LinkLabel.create(PluginInfo.AUTHOR, new UrlRunnable(PluginInfo.GITHUB));
        homeLabel = LinkLabel.create(PluginInfo.HOME_PAGE, new UrlRunnable(PluginInfo.HOME_PAGE));
        paypalLabel = LinkLabel.create("Paypal", new UrlRunnable(PluginInfo.PAYPAL_LINK));
        alipayLabel = LinkLabel.create("Alipay", new UrlRunnable(PluginInfo.ALIPAY_LINK));
    }
}
