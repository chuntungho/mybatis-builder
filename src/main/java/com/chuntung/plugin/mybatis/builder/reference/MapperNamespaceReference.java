/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.reference;

import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.util.IncorrectOperationException;

public class MapperNamespaceReference extends PsiReferenceBase<XmlAttributeValue> {

    public MapperNamespaceReference(@NotNull XmlAttributeValue element) {
        super(element, ElementManipulators.getValueTextRange(element), false);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return MapperXmlIndex.findMapperInterface(getElement().getProject(), getValue());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        String newValue;
        if (newElementName.indexOf('.') >= 0) {
            newValue = newElementName;
        } else {
            String currentValue = getValue();
            int dot = currentValue.lastIndexOf('.');
            newValue = dot >= 0 ? currentValue.substring(0, dot + 1) + newElementName : newElementName;
        }
        return ElementManipulators.handleContentChange(getElement(), getRangeInElement(), newValue);
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        if (element instanceof PsiClass) {
            String fqn = ((PsiClass) element).getQualifiedName();
            if (fqn != null) {
                return ElementManipulators.handleContentChange(getElement(), getRangeInElement(), fqn);
            }
        }
        return getElement();
    }
}
