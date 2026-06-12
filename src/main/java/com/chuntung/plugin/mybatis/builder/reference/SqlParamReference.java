/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.reference;

import com.chuntung.plugin.mybatis.builder.util.MapperParamUtil;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference from the first segment of a #{...} / ${...} expression in mapper XML
 * to the corresponding @Param value, method parameter or bean property.
 */
public class SqlParamReference extends PsiReferenceBase<XmlText> {

    public SqlParamReference(@NotNull XmlText element, @NotNull TextRange rangeInElement) {
        // soft: problems are reported by SqlParamInspection, not default XML highlighting
        super(element, rangeInElement, true);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        String name = getRangeInElement().substring(getElement().getText());
        for (MapperParamUtil.ParamBinding binding : MapperParamUtil.resolveBindingsAt(getElement())) {
            if (binding.name.equals(name)) {
                return binding.target;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> variants = new ArrayList<>();
        for (MapperParamUtil.ParamBinding binding : MapperParamUtil.resolveBindingsAt(getElement())) {
            LookupElementBuilder builder = LookupElementBuilder.create(binding.name);
            if (binding.type != null) {
                builder = builder.withTypeText(binding.type.getPresentableText());
            }
            variants.add(builder);
        }
        return variants.toArray();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return ElementManipulators.handleContentChange(getElement(), getRangeInElement(), newElementName);
    }
}
