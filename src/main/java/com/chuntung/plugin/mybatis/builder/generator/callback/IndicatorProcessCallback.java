/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.callback;

import com.intellij.openapi.progress.ProgressIndicator;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.internal.NullProgressCallback;

/**
 * Display process message on progress indicator.
 *
 * @author Tony Ho
 *
 */
public class IndicatorProcessCallback extends NullProgressCallback {
    private ProgressIndicator indicator;

    public IndicatorProcessCallback(ProgressIndicator indicator) {
        this.indicator = indicator;
    }

    @Override
    public void startTask(String taskName) {
        indicator.setText("MyBatis Builder: " + taskName);
    }

    @Override
    public void done() {
        indicator.setText("MyBatis Builder: Generation done");
    }

}
