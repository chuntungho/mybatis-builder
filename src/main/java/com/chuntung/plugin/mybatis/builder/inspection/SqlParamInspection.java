/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.inspection;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderBundle;
import com.chuntung.plugin.mybatis.builder.reference.MapperNamespaceReferenceContributor;
import com.chuntung.plugin.mybatis.builder.util.MapperParamUtil;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reports unknown parameter names in #{...} / ${...} expressions and in
 * if/when test and foreach collection expressions of mapper XML files.
 */
public class SqlParamInspection extends LocalInspectionTool {

    private static final Set<String> OGNL_KEYWORDS = new HashSet<>(Arrays.asList(
            "and", "or", "not", "null", "true", "false", "in", "instanceof", "new"));

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$.]*");

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new XmlElementVisitor() {
            @Override
            public void visitXmlText(XmlText xmlText) {
                XmlTag statementTag = MapperParamUtil.enclosingStatementTag(xmlText);
                if (statementTag == null) return;
                if (MapperParamUtil.isOpenEnded(statementTag)) return;

                Set<String> names = bindingNamesAt(xmlText);
                Matcher matcher = MapperNamespaceReferenceContributor.PARAM_EXPR_PATTERN.matcher(xmlText.getText());
                while (matcher.find()) {
                    String first = MapperParamUtil.firstSegment(matcher.group(1));
                    if (first.isEmpty() || names.contains(first)) continue;
                    int start = matcher.start(1);
                    holder.registerProblem(xmlText,
                            new TextRange(start, start + first.length()),
                            unknownMessage(first, names));
                }
            }

            @Override
            public void visitXmlAttributeValue(XmlAttributeValue value) {
                if (!(value.getParent() instanceof XmlAttribute)) return;
                XmlAttribute attr = (XmlAttribute) value.getParent();
                XmlTag tag = attr.getParent();
                if (tag == null) return;
                String tagName = tag.getName();
                String attrName = attr.getName();

                boolean isTest = "test".equals(attrName) && ("if".equals(tagName) || "when".equals(tagName));
                boolean isCollection = "collection".equals(attrName) && "foreach".equals(tagName);
                if (!isTest && !isCollection) return;

                XmlTag statementTag = MapperParamUtil.enclosingStatementTag(value);
                if (statementTag == null) return;
                if (MapperParamUtil.isOpenEnded(statementTag)) return;

                String text = value.getValue();
                if (text == null || text.isEmpty()) return;
                Set<String> names = bindingNamesAt(value);
                // offset of value text within the attribute value element (skip opening quote)
                int valueOffset = value.getText().indexOf(text);
                if (valueOffset < 0) return;

                if (isCollection) {
                    String first = MapperParamUtil.firstSegment(text);
                    if (!first.isEmpty() && !names.contains(first) && isIdentifier(first)) {
                        int start = text.indexOf(first);
                        if (start >= 0) {
                            holder.registerProblem(value,
                                    new TextRange(valueOffset + start, valueOffset + start + first.length()),
                                    unknownMessage(first, names));
                        }
                    }
                    return;
                }

                // test expression: conservatively flag only simple identifier references
                Matcher matcher = IDENTIFIER_PATTERN.matcher(text);
                while (matcher.find()) {
                    String token = matcher.group();
                    String first = MapperParamUtil.firstSegment(token);
                    if (first.isEmpty()) continue;
                    if (OGNL_KEYWORDS.contains(first)) continue;
                    // skip method calls and tokens preceded by '.' or '@' (static refs, chained calls)
                    int end = matcher.end();
                    if (end < text.length() && text.charAt(end) == '(') continue;
                    int start = matcher.start();
                    if (start > 0) {
                        char prev = text.charAt(start - 1);
                        if (prev == '.' || prev == '@' || prev == '\'' || prev == '"' || prev == '#') continue;
                    }
                    if (names.contains(first)) continue;
                    holder.registerProblem(value,
                            new TextRange(valueOffset + start, valueOffset + start + first.length()),
                            unknownMessage(first, names));
                }
            }
        };
    }

    private static boolean isIdentifier(String s) {
        return Pattern.matches("[A-Za-z_$][A-Za-z0-9_$]*", s);
    }

    private static Set<String> bindingNamesAt(com.intellij.psi.PsiElement context) {
        Set<String> names = new HashSet<>();
        for (MapperParamUtil.ParamBinding binding : MapperParamUtil.resolveBindingsAt(context)) {
            names.add(binding.name);
        }
        return names;
    }

    private static String unknownMessage(String name, Set<String> names) {
        List<String> sorted = new ArrayList<>(names);
        sorted.sort(String::compareTo);
        String available = sorted.isEmpty() ? "(none)" : String.join(", ", sorted);
        return MybatisBuilderBundle.message("inspection.unresolved.param", name, available);
    }
}
