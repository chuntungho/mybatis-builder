/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderService;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorParamWrapper;
import com.chuntung.plugin.mybatis.builder.generator.GeneratorToolWrapper;
import com.intellij.openapi.project.Project;

import java.io.File;
import java.io.IOException;

public class ParametersHandler {
    private final MybatisBuilderService service;

    public ParametersHandler(Project project) {
        service = MybatisBuilderService.getInstance(project);
    }

    public static ParametersHandler getInstance(Project project) {
        return new ParametersHandler(project);
    }

    public void stashGeneratorParamWrapper(GeneratorParamWrapper paramWrapper){
        service.stashGeneratorParamWrapper(paramWrapper);
    }

    public void exportConfiguration(GeneratorParamWrapper paramWrapper, File file) throws IOException {
        new GeneratorToolWrapper(paramWrapper, null).export(file);
    }

}
