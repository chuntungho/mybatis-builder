/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.completion;

import com.chuntung.plugin.mybatis.builder.util.MapperParamUtil;
import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Completion for mapper XML files: #{}/${} parameters (including one level of
 * bean properties), if/when test and foreach collection expressions, resultMap
 * and include refid ids across same-namespace files.
 */
public class MapperXmlCompletionContributor extends CompletionContributor {

    public MapperXmlCompletionContributor() {
        // 1. #{} and ${} in SQL text
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(XmlPatterns.xmlText()),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();
                        XmlText xmlText = PsiTreeUtil.getParentOfType(position, XmlText.class, false);
                        if (xmlText == null) return;
                        if (MapperParamUtil.enclosingStatementTag(xmlText) == null) return;

                        int caretInText = parameters.getOffset() - xmlText.getTextRange().getStartOffset();
                        String text = xmlText.getText();
                        if (caretInText < 0 || caretInText > text.length()) return;
                        String prefix = unclosedParamPrefix(text, caretInText);
                        if (prefix == null) return;

                        CompletionResultSet rs = result.withPrefixMatcher(prefix);
                        for (MapperParamUtil.ParamBinding binding : MapperParamUtil.resolveBindingsAt(xmlText)) {
                            String typeText = binding.type != null ? binding.type.getPresentableText() : "";
                            rs.addElement(LookupElementBuilder.create(binding.name)
                                    .withTypeText(typeText, true)
                                    .withIcon(AllIcons.Nodes.Parameter));
                            // one level of bean properties for non-simple class types
                            if (binding.type instanceof PsiClassType && !MapperParamUtil.isSimpleType(binding.type)) {
                                PsiClass cls = ((PsiClassType) binding.type).resolve();
                                if (cls != null && cls.getQualifiedName() != null
                                        && !cls.getQualifiedName().startsWith("java.")) {
                                    for (PsiField field : cls.getAllFields()) {
                                        if (field.hasModifierProperty(PsiModifier.STATIC)) continue;
                                        rs.addElement(LookupElementBuilder
                                                .create(binding.name + "." + field.getName())
                                                .withTypeText(field.getType().getPresentableText(), true)
                                                .withIcon(AllIcons.Nodes.Parameter));
                                    }
                                }
                            }
                        }
                    }
                });

        // 2. test attribute of if/when, collection attribute of foreach
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(
                        XmlPatterns.xmlAttributeValue().withSuperParent(2,
                                XmlPatterns.xmlTag().withLocalName(
                                        StandardPatterns.string().oneOf("if", "when", "foreach")))),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();
                        XmlAttribute attr = PsiTreeUtil.getParentOfType(position, XmlAttribute.class);
                        if (attr == null) return;
                        String attrName = attr.getName();
                        if (!"test".equals(attrName) && !"collection".equals(attrName)) return;
                        if (MapperParamUtil.enclosingStatementTag(position) == null) return;

                        for (MapperParamUtil.ParamBinding binding : MapperParamUtil.resolveBindingsAt(position)) {
                            String typeText = binding.type != null ? binding.type.getPresentableText() : "";
                            result.addElement(LookupElementBuilder.create(binding.name)
                                    .withTypeText(typeText, true)
                                    .withIcon(AllIcons.Nodes.Parameter));
                        }
                        if ("test".equals(attrName)) {
                            for (String keyword : new String[]{"and", "or", "not", "null"}) {
                                result.addElement(LookupElementBuilder.create(keyword).bold());
                            }
                        }
                    }
                });

        // 3. resultMap attribute on select tags
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(
                        XmlPatterns.xmlAttributeValue().withSuperParent(2,
                                XmlPatterns.xmlTag().withLocalName("select"))),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        addIdCompletions(parameters, "resultMap", "resultMap", result);
                    }
                });

        // 4. refid attribute on include tags
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().inside(
                        XmlPatterns.xmlAttributeValue().withSuperParent(2,
                                XmlPatterns.xmlTag().withLocalName("include"))),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        addIdCompletions(parameters, "refid", "sql", result);
                    }
                });
    }

    /**
     * Returns the text between an unclosed "#{" / "${" and the caret, or null when
     * the caret is not inside an unclosed parameter expression.
     */
    private static String unclosedParamPrefix(String text, int caretInText) {
        for (int i = caretInText - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '}') return null;
            if (c == '{' && i > 0 && (text.charAt(i - 1) == '#' || text.charAt(i - 1) == '$')) {
                return text.substring(i + 1, caretInText).trim();
            }
            if (c == '{') return null;
        }
        return null;
    }

    private static void addIdCompletions(CompletionParameters parameters, String attrName,
                                         String targetTagName, CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        XmlAttribute attr = PsiTreeUtil.getParentOfType(position, XmlAttribute.class);
        if (attr == null || !attrName.equals(attr.getName())) return;
        PsiFile original = parameters.getOriginalFile();
        if (!(original instanceof XmlFile)) return;
        for (String id : collectIds((XmlFile) original, targetTagName)) {
            result.addElement(LookupElementBuilder.create(id).withIcon(AllIcons.Nodes.Tag));
        }
    }

    /**
     * Collects ids of the given tag name from the origin file and all files
     * sharing its namespace.
     */
    private static Set<String> collectIds(XmlFile origin, String tagName) {
        Set<String> ids = new LinkedHashSet<>();
        List<XmlFile> files = new ArrayList<>();
        files.add(origin);
        String namespace = MapperXmlIndex.readNamespace(origin);
        if (namespace != null) {
            for (XmlFile sibling : MapperXmlIndex.findXmlMapperFiles(origin.getProject(), namespace)) {
                if (!sibling.equals(origin)) files.add(sibling);
            }
        }
        for (XmlFile file : files) {
            XmlTag root = file.getRootTag();
            if (root == null || !"mapper".equals(root.getName())) continue;
            for (XmlTag child : root.getSubTags()) {
                if (tagName.equals(child.getName())) {
                    String id = child.getAttributeValue("id");
                    if (id != null && !id.isEmpty()) ids.add(id);
                }
            }
        }
        return ids;
    }
}
