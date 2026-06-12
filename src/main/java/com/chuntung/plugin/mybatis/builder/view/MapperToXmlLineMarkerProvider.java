/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.view;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderBundle;
import com.chuntung.plugin.mybatis.builder.MybatisIcons;
import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MapperToXmlLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiIdentifier)) return;
        PsiElement parent = element.getParent();
        if (parent instanceof PsiClass) {
            collectForClass((PsiIdentifier) element, (PsiClass) parent, result);
        } else if (parent instanceof PsiMethod) {
            collectForMethod((PsiIdentifier) element, (PsiMethod) parent, result);
        }
    }

    private void collectForClass(PsiIdentifier identifier,
                                 PsiClass psiClass,
                                 Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!psiClass.isInterface()) return;
        String fqn = psiClass.getQualifiedName();
        if (fqn == null) return;
        List<XmlFile> mappers = MapperXmlIndex.findXmlMapperFiles(psiClass.getProject(), fqn);
        if (mappers.isEmpty()) return;
        List<XmlTag> targets = new ArrayList<>();
        for (XmlFile xml : mappers) {
            XmlTag root = xml.getRootTag();
            if (root != null) targets.add(root);
        }
        result.add(NavigationGutterIconBuilder.create(MybatisIcons.MAPPER_TO_XML)
                .setTargets(targets)
                .setTooltipText(MybatisBuilderBundle.message("tooltip.navigate.to.xml.mapper"))
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createLineMarkerInfo(identifier));
    }

    private void collectForMethod(PsiIdentifier identifier,
                                  PsiMethod method,
                                  Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        PsiClass containing = method.getContainingClass();
        if (containing == null || !containing.isInterface()) return;
        String fqn = containing.getQualifiedName();
        if (fqn == null) return;
        List<XmlFile> mappers = MapperXmlIndex.findXmlMapperFiles(method.getProject(), fqn);
        if (mappers.isEmpty()) return;
        List<XmlTag> targets = new ArrayList<>();
        for (XmlFile xml : mappers) {
            XmlTag tag = MapperXmlIndex.findXmlStatement(xml, method.getName());
            if (tag != null) targets.add(tag);
        }
        if (targets.isEmpty()) return;
        result.add(NavigationGutterIconBuilder.create(MybatisIcons.MAPPER_TO_XML)
                .setTargets(targets)
                .setTooltipText(MybatisBuilderBundle.message("tooltip.navigate.to.xml.statement"))
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createLineMarkerInfo(identifier));
    }
}
