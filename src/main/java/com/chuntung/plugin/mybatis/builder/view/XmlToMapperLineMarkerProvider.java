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
import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class XmlToMapperLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element,
                                            @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof XmlToken)) return;
        XmlToken token = (XmlToken) element;
        if (token.getTokenType() != XmlTokenType.XML_NAME) return;
        PsiElement parent = token.getParent();
        if (!(parent instanceof XmlTag)) return;
        XmlTag tag = (XmlTag) parent;
        if (!isStartTagNameToken(token, tag)) return;
        if (!(tag.getContainingFile() instanceof XmlFile)) return;
        XmlFile xmlFile = (XmlFile) tag.getContainingFile();

        if ("mapper".equals(tag.getName()) && tag.getParentTag() == null) {
            handleRoot(element, xmlFile, result);
        } else if (MapperXmlIndex.STATEMENT_TAG_NAMES.contains(tag.getName())
                && tag.getParentTag() != null
                && "mapper".equals(tag.getParentTag().getName())) {
            handleStatement(element, tag, xmlFile, result);
        }
    }

    private void handleRoot(PsiElement anchor,
                            XmlFile xmlFile,
                            Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        String ns = MapperXmlIndex.readNamespace(xmlFile);
        if (ns == null) return;
        PsiClass cls = MapperXmlIndex.findMapperInterface(xmlFile.getProject(), ns);
        if (cls == null) return;
        PsiElement target = cls.getNameIdentifier() != null ? cls.getNameIdentifier() : cls;
        result.add(NavigationGutterIconBuilder.create(MybatisIcons.XML_TO_MAPPER)
                .setTargets(Collections.singletonList(target))
                .setTooltipText(MybatisBuilderBundle.message("tooltip.navigate.to.mapper.interface"))
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createLineMarkerInfo(anchor));
    }

    private void handleStatement(PsiElement anchor,
                                 XmlTag tag,
                                 XmlFile xmlFile,
                                 Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        String id = tag.getAttributeValue("id");
        if (id == null || id.isEmpty()) return;
        String ns = MapperXmlIndex.readNamespace(xmlFile);
        if (ns == null) return;
        PsiClass cls = MapperXmlIndex.findMapperInterface(xmlFile.getProject(), ns);
        PsiMethod method = MapperXmlIndex.findMapperMethod(cls, id);
        if (method == null) return;
        PsiElement target = method.getNameIdentifier() != null ? method.getNameIdentifier() : method;
        result.add(NavigationGutterIconBuilder.create(MybatisIcons.XML_TO_MAPPER)
                .setTargets(Collections.singletonList(target))
                .setTooltipText(MybatisBuilderBundle.message("tooltip.navigate.to.mapper.method"))
                .setAlignment(GutterIconRenderer.Alignment.RIGHT)
                .createLineMarkerInfo(anchor));
    }

    private boolean isStartTagNameToken(XmlToken token, XmlTag tag) {
        PsiElement child = tag.getFirstChild();
        while (child != null) {
            if (child instanceof XmlToken) {
                XmlToken childToken = (XmlToken) child;
                if (childToken.getTokenType() == XmlTokenType.XML_NAME) {
                    return childToken == token;
                }
                if (childToken.getTokenType() == XmlTokenType.XML_TAG_END
                        || childToken.getTokenType() == XmlTokenType.XML_EMPTY_ELEMENT_END) {
                    return false;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }
}
