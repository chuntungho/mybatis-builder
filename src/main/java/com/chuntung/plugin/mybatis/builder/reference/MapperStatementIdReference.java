/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.reference;

import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MapperStatementIdReference extends PsiReferenceBase<XmlAttributeValue> {

    public MapperStatementIdReference(@NotNull XmlAttributeValue element) {
        super(element, ElementManipulators.getValueTextRange(element), false);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiMethod method = resolveMethod();
        return method;
    }

    @Nullable
    private PsiMethod resolveMethod() {
        XmlAttributeValue value = getElement();
        String id = value.getValue();
        if (id == null || id.isEmpty()) return null;
        PsiFile file = value.getContainingFile();
        if (!(file instanceof XmlFile)) return null;
        XmlFile xmlFile = (XmlFile) file;
        String ns = MapperXmlIndex.readNamespace(xmlFile);
        if (ns == null) return null;
        PsiClass cls = MapperXmlIndex.findMapperInterface(value.getProject(), ns);
        return MapperXmlIndex.findMapperMethod(cls, id);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return ElementManipulators.handleContentChange(getElement(), getRangeInElement(), newElementName);
    }
}
