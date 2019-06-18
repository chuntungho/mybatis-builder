/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.model.PluginInfo;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
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

    public AboutDialog(Project project) {
        super(project, false);
        setOKButtonText("Close");
        setTitle("MyBatis Builder - About");
        setButtonsAlignment(SwingConstants.CENTER);

        Cursor hand = new Cursor(Cursor.HAND_CURSOR);

        pluginLabel.setText(PluginInfo.PLUGIN_NAME + " v" + PluginInfo.PLUGIN_VERSION);

        authorLabel.setCursor(hand);
        authorLabel.setText(PluginInfo.AUTHOR);
        authorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.GITHUB);
            }
        });

        homeLabel.setCursor(hand);
        homeLabel.setText(PluginInfo.HOME_PAGE);
        homeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.HOME_PAGE);
            }
        });

        paypalLabel.setCursor(hand);
        paypalLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.browse(PluginInfo.PAYPAL_LINK);
            }
        });

        alipayLabel.setCursor(hand);
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
