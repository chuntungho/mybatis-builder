/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.reference;

import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Reference from a resultMap / refid attribute value to the corresponding
 * &lt;resultMap&gt; / &lt;sql&gt; declaration in the same file or same-namespace files.
 */
public class MapperXmlIdReference extends PsiReferenceBase<XmlAttributeValue> {

    private final String targetTagName;

    public MapperXmlIdReference(@NotNull XmlAttributeValue element, @NotNull String targetTagName) {
        super(element, ElementManipulators.getValueTextRange(element), true);
        this.targetTagName = targetTagName;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        String id = getElement().getValue();
        if (id == null || id.isEmpty()) return null;
        for (XmlFile xmlFile : candidateFiles()) {
            XmlTag root = xmlFile.getRootTag();
            if (root == null || !"mapper".equals(root.getName())) continue;
            for (XmlTag child : root.getSubTags()) {
                if (targetTagName.equals(child.getName()) && id.equals(child.getAttributeValue("id"))) {
                    XmlAttribute idAttr = child.getAttribute("id");
                    return idAttr != null ? idAttr.getValueElement() : child;
                }
            }
        }
        return null;
    }

    @NotNull
    private List<XmlFile> candidateFiles() {
        List<XmlFile> files = new ArrayList<>();
        PsiFile file = getElement().getContainingFile();
        if (!(file instanceof XmlFile)) return files;
        XmlFile origin = (XmlFile) file;
        files.add(origin);
        String namespace = MapperXmlIndex.readNamespace(origin);
        if (namespace != null) {
            for (XmlFile sibling : MapperXmlIndex.findXmlMapperFiles(getElement().getProject(), namespace)) {
                if (!sibling.equals(origin)) files.add(sibling);
            }
        }
        return files;
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
