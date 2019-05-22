/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatisbuilder.view;

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
    private JButton paypalButton;
    private JButton alipayButton;

    public AboutDialog(Project project) {
        super(project, false);
        setOKButtonText("Close");
        setTitle("Mybatis Builder - About");
        setButtonsAlignment(SwingConstants.CENTER);

        authorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                BrowserUtil.browse(label.getText());
            }
        });

        paypalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse("https://www.paypal.me/chuntungho?locale.x=en_US");
            }
        });

        alipayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BrowserUtil.browse("https://chuntung.com/donate/");
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
