/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.reference;

import com.chuntung.plugin.mybatis.builder.util.MapperParamUtil;
import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapperNamespaceReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue()
                        .withSuperParent(1, XmlPatterns.xmlAttribute().withName("namespace"))
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName("mapper")),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        return new PsiReference[]{new MapperNamespaceReference((XmlAttributeValue) element)};
                    }
                });

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue()
                        .withSuperParent(1, XmlPatterns.xmlAttribute().withName("id"))
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(
                                StandardPatterns.string().oneOf(
                                        MapperXmlIndex.REFERENCEABLE_TAG_NAMES.toArray(new String[0])))),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        XmlTag tag = enclosingTagWithMapperParent(element);
                        if (tag == null) return PsiReference.EMPTY_ARRAY;
                        return new PsiReference[]{new MapperStatementIdReference((XmlAttributeValue) element)};
                    }
                });

        JavaClassReferenceProvider classRefProvider = new JavaClassReferenceProvider();
        classRefProvider.setSoft(true);

        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue()
                        .withSuperParent(1, XmlPatterns.xmlAttribute().withName(
                                StandardPatterns.string().oneOf("parameterType", "resultType", "type")))
                        .withSuperParent(2, XmlPatterns.xmlTag().withLocalName(
                                StandardPatterns.string().oneOf(
                                        "select", "insert", "update", "delete", "resultMap"))),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        XmlTag tag = enclosingTagWithMapperParent(element);
                        if (tag == null) return PsiReference.EMPTY_ARRAY;
                        return classRefProvider.getReferencesByElement(element);
                    }
                });

        // #{...} / ${...} parameter references in SQL text
        registrar.registerReferenceProvider(
                XmlPatterns.xmlText(),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        if (!(element instanceof XmlText)) return PsiReference.EMPTY_ARRAY;
                        if (MapperParamUtil.enclosingStatementTag(element) == null) return PsiReference.EMPTY_ARRAY;
                        XmlText xmlText = (XmlText) element;
                        List<PsiReference> refs = new ArrayList<>();
                        Matcher matcher = PARAM_EXPR_PATTERN.matcher(xmlText.getText());
                        while (matcher.find()) {
                            String expr = matcher.group(1);
                            String first = MapperParamUtil.firstSegment(expr);
                            if (first.isEmpty()) continue;
                            int start = matcher.start(1);
                            refs.add(new SqlParamReference(xmlText, new TextRange(start, start + first.length())));
                        }
                        return refs.toArray(PsiReference.EMPTY_ARRAY);
                    }
                });

        // resultMap / refid id references
        registrar.registerReferenceProvider(
                XmlPatterns.xmlAttributeValue()
                        .withSuperParent(1, XmlPatterns.xmlAttribute().withName(
                                StandardPatterns.string().oneOf("resultMap", "refid"))),
                new PsiReferenceProvider() {
                    @NotNull
                    @Override
                    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                                 @NotNull ProcessingContext context) {
                        XmlAttributeValue value = (XmlAttributeValue) element;
                        if (!(value.getParent() instanceof XmlAttribute)) return PsiReference.EMPTY_ARRAY;
                        XmlAttribute attr = (XmlAttribute) value.getParent();
                        XmlTag tag = attr.getParent();
                        if (tag == null) return PsiReference.EMPTY_ARRAY;
                        String targetTagName;
                        if ("resultMap".equals(attr.getName()) && "select".equals(tag.getName())) {
                            targetTagName = "resultMap";
                        } else if ("refid".equals(attr.getName()) && "include".equals(tag.getName())) {
                            targetTagName = "sql";
                        } else {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        return new PsiReference[]{new MapperXmlIdReference(value, targetTagName)};
                    }
                });
    }

    public static final Pattern PARAM_EXPR_PATTERN = Pattern.compile("[#$]\\{\\s*([A-Za-z0-9_$.]+)");

    private static XmlTag enclosingTagWithMapperParent(PsiElement attributeValue) {
        PsiElement attr = attributeValue.getParent();
        PsiElement maybeTag = attr != null ? attr.getParent() : null;
        if (!(maybeTag instanceof XmlTag)) return null;
        XmlTag tag = (XmlTag) maybeTag;
        XmlTag parent = tag.getParentTag();
        if (parent == null || !"mapper".equals(parent.getName())) return null;
        return tag;
    }
}
