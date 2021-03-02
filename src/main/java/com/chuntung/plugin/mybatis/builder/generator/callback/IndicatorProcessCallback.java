/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.callback;

import com.intellij.openapi.progress.ProgressIndicator;
import org.mybatis.generator.api.ProgressCallback;

/**
 * Display process message on progress indicator.
 *
 * @author Tony Ho
 */
public class IndicatorProcessCallback implements ProgressCallback {
    private ProgressIndicator indicator;

    public IndicatorProcessCallback(ProgressIndicator indicator) {
        this.indicator = indicator;
    }

    @Override
    public void introspectionStarted(int totalTasks) {

    }

    @Override
    public void generationStarted(int totalTasks) {

    }

    @Override
    public void saveStarted(int totalTasks) {

    }

    @Override
    public void startTask(String taskName) {
        indicator.setText("MyBatis Builder: " + taskName);
    }

    @Override
    public void done() {
        indicator.setText("MyBatis Builder: Generation done");
    }

    @Override
    public void checkCancel() throws InterruptedException {

    }

}
