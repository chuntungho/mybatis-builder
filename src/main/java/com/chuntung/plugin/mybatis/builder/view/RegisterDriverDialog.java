/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderBundle;
import com.chuntung.plugin.mybatis.builder.database.DriverDownloader;
import com.chuntung.plugin.mybatis.builder.model.CustomDriverInfo;
import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages the list of user-registered JDBC drivers (name, driver class, URL template,
 * driver jar, default port). The caller persists the edited list returned by
 * {@link #getDrivers()} after the dialog is accepted.
 */
public class RegisterDriverDialog extends DialogWrapper {
    private static final FileChooserDescriptor JAR_DESCRIPTOR =
            new FileChooserDescriptor(false, false, true, false, false, false);

    private final DefaultListModel<CustomDriverInfo> listModel = new DefaultListModel<>();
    private final JBList<CustomDriverInfo> driverList = new JBList<>(listModel);

    private final JBTextField nameField = new JBTextField();
    private final JBTextField classField = new JBTextField();
    private final JBTextField urlField = new JBTextField();
    private final JBTextField mavenCoordField = new JBTextField();
    private JButton downloadButton;
    private final TextFieldWithBrowseButton libraryField = new TextFieldWithBrowseButton();
    private final JBTextField portField = new JBTextField();

    private final Project project;
    private CustomDriverInfo currentDriver;
    private boolean loadingForm;

    public RegisterDriverDialog(@Nullable Project project, List<CustomDriverInfo> drivers) {
        super(project);
        this.project = project;
        setTitle(MybatisBuilderBundle.message("dialog.register.driver.title"));

        for (CustomDriverInfo driver : drivers) {
            listModel.addElement(driver.clone());
        }

        driverList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        driverList.addListSelectionListener(this::onSelectionChanged);
        libraryField.addBrowseFolderListener(MybatisBuilderBundle.message("choose.driver.library"),
                MybatisBuilderBundle.message("choose.driver.library.description"), project, JAR_DESCRIPTOR);

        downloadButton = new JButton(MybatisBuilderBundle.message("button.download"));
        downloadButton.addActionListener(e -> doDownload());

        init();

        if (!listModel.isEmpty()) {
            driverList.setSelectedIndex(0);
        } else {
            setFormEnabled(false);
        }
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel listPanel = ToolbarDecorator.createDecorator(driverList)
                .setAddAction(b -> addDriver())
                .setRemoveAction(b -> removeDriver())
                .createPanel();
        listPanel.setPreferredSize(new Dimension(180, 320));

        JPanel mavenPanel = new JPanel(new BorderLayout(4, 0));
        mavenPanel.add(mavenCoordField, BorderLayout.CENTER);
        mavenPanel.add(downloadButton, BorderLayout.EAST);

        JPanel form = FormBuilder.createFormBuilder()
                .addLabeledComponent(MybatisBuilderBundle.message("label.name"), nameField)
                .addLabeledComponent(MybatisBuilderBundle.message("label.driver.class"), classField)
                .addLabeledComponent(MybatisBuilderBundle.message("label.url.template"), urlField)
                .addLabeledComponent(MybatisBuilderBundle.message("label.maven.coordinate"), mavenPanel)
                .addLabeledComponent(MybatisBuilderBundle.message("label.driver.library"), libraryField)
                .addLabeledComponent(MybatisBuilderBundle.message("label.default.port"), portField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        form.setBorder(JBUI.Borders.emptyLeft(8));
        form.setPreferredSize(new Dimension(380, 320));

        JPanel content = new JPanel(new BorderLayout());
        content.add(listPanel, BorderLayout.WEST);
        content.add(form, BorderLayout.CENTER);
        return content;
    }

    private void onSelectionChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        flushForm();
        currentDriver = driverList.getSelectedValue();
        loadForm(currentDriver);
    }

    private void addDriver() {
        flushForm();
        CustomDriverInfo driver = new CustomDriverInfo();
        driver.setId(UUID.randomUUID().toString().replace("-", ""));
        driver.setName(MybatisBuilderBundle.message("message.new.driver"));
        listModel.addElement(driver);
        driverList.setSelectedIndex(listModel.size() - 1);
    }

    private void removeDriver() {
        int idx = driverList.getSelectedIndex();
        if (idx < 0) {
            return;
        }
        currentDriver = null; // avoid flushing into the row being removed
        listModel.remove(idx);
        if (listModel.isEmpty()) {
            loadForm(null);
            setFormEnabled(false);
        } else {
            driverList.setSelectedIndex(Math.min(idx, listModel.size() - 1));
        }
    }

    private void loadForm(CustomDriverInfo driver) {
        loadingForm = true;
        try {
            setFormEnabled(driver != null);
            nameField.setText(driver == null ? "" : nullToEmpty(driver.getName()));
            classField.setText(driver == null ? "" : nullToEmpty(driver.getDriverClass()));
            urlField.setText(driver == null ? "" : nullToEmpty(driver.getUrlPattern()));
            mavenCoordField.setText(driver == null ? "" : nullToEmpty(driver.getMavenCoordinate()));
            libraryField.setText(driver == null ? "" : nullToEmpty(driver.getDriverLibrary()));
            portField.setText(driver == null || driver.getDefaultPort() == null
                    ? "" : String.valueOf(driver.getDefaultPort()));
        } finally {
            loadingForm = false;
        }
    }

    private void flushForm() {
        if (loadingForm || currentDriver == null) {
            return;
        }
        currentDriver.setName(nameField.getText().trim());
        currentDriver.setDriverClass(classField.getText().trim());
        currentDriver.setUrlPattern(urlField.getText().trim());
        currentDriver.setMavenCoordinate(mavenCoordField.getText().trim());
        currentDriver.setDriverLibrary(libraryField.getText().trim());
        currentDriver.setDefaultPort(parsePort(portField.getText()));
        // refresh the list label in case the name changed
        driverList.repaint();
    }

    private void setFormEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        classField.setEnabled(enabled);
        urlField.setEnabled(enabled);
        mavenCoordField.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        libraryField.setEnabled(enabled);
        portField.setEnabled(enabled);
    }

    @Nullable
    @Override
    protected ValidationInfo doValidate() {
        flushForm();
        for (int i = 0; i < listModel.size(); i++) {
            CustomDriverInfo d = listModel.get(i);
            if (!StringUtil.stringHasValue(d.getName())) {
                return new ValidationInfo(MybatisBuilderBundle.message("validation.driver.name.required"), nameField);
            }
            if (!StringUtil.stringHasValue(d.getDriverClass())) {
                return new ValidationInfo(MybatisBuilderBundle.message("validation.driver.class.required", d.getName()), classField);
            }
            boolean hasLibrary = StringUtil.stringHasValue(d.getDriverLibrary());
            boolean hasCoordinate = StringUtil.stringHasValue(d.getMavenCoordinate());
            if (!hasLibrary && !hasCoordinate) {
                return new ValidationInfo(
                        MybatisBuilderBundle.message("validation.driver.library.required", d.getName()),
                        libraryField);
            }
        }
        return null;
    }

    private void doDownload() {
        flushForm();
        if (currentDriver == null) return;
        String coord = currentDriver.getMavenCoordinate();
        if (!StringUtil.stringHasValue(coord)) {
            Messages.showWarningDialog(project, MybatisBuilderBundle.message("warning.no.coordinate.message"), MybatisBuilderBundle.message("warning.no.coordinate.title"));
            return;
        }
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MybatisBuilderBundle.message("message.downloading.driver"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    DriverDownloader.getInstance().download(coord, indicator);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
            @Override
            public void onSuccess() {
                Path jar = DriverDownloader.getInstance().localJar(coord);
                if (jar != null && currentDriver != null) {
                    currentDriver.setDriverLibrary(jar.toString());
                    loadForm(currentDriver);
                    driverList.repaint();
                }
            }
            @Override
            public void onThrowable(@NotNull Throwable error) {
                Messages.showErrorDialog(project, error.getMessage(), MybatisBuilderBundle.message("error.driver.download.failed"));
            }
        });
    }

    /**
     * The edited drivers; valid only after the dialog is accepted (OK).
     */
    public List<CustomDriverInfo> getDrivers() {
        flushForm();
        List<CustomDriverInfo> result = new ArrayList<>(listModel.size());
        for (int i = 0; i < listModel.size(); i++) {
            result.add(listModel.get(i));
        }
        return result;
    }

    @Override
    protected String getDimensionServiceKey() {
        return "MyBatisBuilder.RegisterDriverDialog";
    }

    private static Integer parsePort(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
