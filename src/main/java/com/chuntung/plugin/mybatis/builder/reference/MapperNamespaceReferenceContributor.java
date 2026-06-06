/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.reference;

import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

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
    }

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
