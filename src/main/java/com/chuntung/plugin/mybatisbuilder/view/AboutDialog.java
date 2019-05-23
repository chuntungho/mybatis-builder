/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.view;

import com.chuntung.plugin.mybatisbuilder.model.PluginInfo;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AboutDialog extends DialogWrapper {
    private JPanel contentPanel;
    private JLabel authorLabel;
    private JLabel pluginLabel;
    private JLabel homeLabel;
    private JLabel paypalLabel;
    private JLabel alipayLabel;

    public AboutDialog(Project project) {
        super(project, false);
        setOKButtonText("Close");
        setTitle("Mybatis Builder - About");
        setButtonsAlignment(SwingConstants.CENTER);

        pluginLabel.setText(PluginInfo.PLUGIN_NAME + " v" + PluginInfo.PLUGIN_VERSION);

        authorLabel.setText(PluginInfo.AUTHOR);
        authorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.GITHUB);
            }
        });

        homeLabel.setText(PluginInfo.HOME_PAGE);
        homeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.HOME_PAGE);
            }
        });

        paypalLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.PAYPAL_LINK);
            }
        });

        alipayLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.ALIPAY_LINK);
            }
        });

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
}
