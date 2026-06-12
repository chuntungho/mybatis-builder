/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Computes the parameter bindings available inside a mapper statement tag.
 */
public final class MapperParamUtil {

    private static final String PARAM_ANNOTATION = "org.apache.ibatis.annotations.Param";

    private static final Set<String> IGNORED_PARAM_TYPES = new HashSet<>(Arrays.asList(
            "org.apache.ibatis.session.RowBounds",
            "org.apache.ibatis.session.ResultHandler"));

    private static final Set<String> SIMPLE_TYPE_FQNS = new HashSet<>(Arrays.asList(
            "java.lang.String", "java.lang.Byte", "java.lang.Character", "java.lang.Short",
            "java.lang.Integer", "java.lang.Long", "java.lang.Float", "java.lang.Double",
            "java.lang.Boolean", "java.util.Date", "java.math.BigDecimal", "java.math.BigInteger"));

    private MapperParamUtil() {}

    /** One available binding inside #{...} / test / foreach expressions. */
    public static class ParamBinding {
        /** Binding name as typed in #{}. */
        public final String name;
        /** Navigation/rename target (PsiParameter, or property member). */
        public final PsiElement target;
        /** May be null. */
        public final PsiType type;

        public ParamBinding(@NotNull String name, @NotNull PsiElement target, @Nullable PsiType type) {
            this.name = name;
            this.target = target;
            this.type = type;
        }
    }

    /**
     * Resolves the mapper method backing the given statement tag.
     */
    @Nullable
    public static PsiMethod resolveStatementMethod(@Nullable XmlTag statementTag) {
        if (statementTag == null) return null;
        XmlTag root = statementTag.getParentTag();
        while (root != null && root.getParentTag() != null) {
            root = root.getParentTag();
        }
        if (root == null || !"mapper".equals(root.getName())) return null;
        String namespace = root.getAttributeValue("namespace");
        if (namespace == null || namespace.isEmpty()) return null;
        String id = statementTag.getAttributeValue("id");
        if (id == null || id.isEmpty()) return null;
        PsiClass iface = MapperXmlIndex.findMapperInterface(statementTag.getProject(), namespace);
        return MapperXmlIndex.findMapperMethod(iface, id);
    }

    /**
     * Walks up the PSI tree to the nearest statement tag (select/insert/update/delete)
     * whose parent chain reaches the mapper root.
     */
    @Nullable
    public static XmlTag enclosingStatementTag(@Nullable PsiElement element) {
        PsiElement current = element;
        while (current != null && !(current instanceof XmlFile)) {
            if (current instanceof XmlTag) {
                XmlTag tag = (XmlTag) current;
                if (MapperXmlIndex.STATEMENT_TAG_NAMES.contains(tag.getName()) && reachesMapperRoot(tag)) {
                    return tag;
                }
            }
            current = current.getParent();
        }
        return null;
    }

    private static boolean reachesMapperRoot(XmlTag tag) {
        XmlTag parent = tag.getParentTag();
        while (parent != null) {
            if (parent.getParentTag() == null) {
                return "mapper".equals(parent.getName());
            }
            parent = parent.getParentTag();
        }
        return false;
    }

    /**
     * Returns true when the available bindings cannot be statically determined
     * (single Map parameter, or the mapper method is unresolvable).
     */
    public static boolean isOpenEnded(@Nullable XmlTag statementTag) {
        PsiMethod method = resolveStatementMethod(statementTag);
        if (method == null) return true;
        List<PsiParameter> params = effectiveParameters(method);
        if (params.size() == 1 && findParamAnnotation(params.get(0)) == null) {
            PsiClass cls = resolveClass(params.get(0).getType());
            if (cls != null && InheritanceUtil.isInheritor(cls, "java.util.Map")) return true;
            if (cls != null && "java.util.Map".equals(cls.getQualifiedName())) return true;
        }
        return false;
    }

    /**
     * Bindings available inside the given statement tag (without foreach variables).
     */
    @NotNull
    public static List<ParamBinding> resolveBindings(@Nullable XmlTag statementTag) {
        PsiMethod method = resolveStatementMethod(statementTag);
        if (method == null) return Collections.emptyList();

        List<PsiParameter> params = effectiveParameters(method);
        List<ParamBinding> bindings = new ArrayList<>();

        if (params.size() == 1 && findParamAnnotation(params.get(0)) == null) {
            PsiParameter single = params.get(0);
            PsiType type = single.getType();
            if (isSimpleType(type)) {
                bindings.add(new ParamBinding(single.getName(), single, type));
                return bindings;
            }
            PsiClass cls = resolveClass(type);
            if (cls != null) {
                if ("java.util.Map".equals(cls.getQualifiedName())
                        || InheritanceUtil.isInheritor(cls, "java.util.Map")) {
                    // open-ended: bindings unknown
                    return Collections.emptyList();
                }
                // bean: expand properties
                for (PsiField field : cls.getAllFields()) {
                    if (field.hasModifierProperty(com.intellij.psi.PsiModifier.STATIC)) continue;
                    bindings.add(new ParamBinding(field.getName(), field, field.getType()));
                }
                return bindings;
            }
            bindings.add(new ParamBinding(single.getName(), single, type));
            return bindings;
        }

        for (PsiParameter param : params) {
            PsiAnnotation anno = findParamAnnotation(param);
            if (anno != null) {
                PsiAnnotationMemberValue value = anno.findAttributeValue("value");
                String name = null;
                if (value instanceof PsiLiteralExpression) {
                    Object v = ((PsiLiteralExpression) value).getValue();
                    if (v instanceof String) name = (String) v;
                }
                if (name != null && !name.isEmpty() && value != null) {
                    bindings.add(new ParamBinding(name, value, param.getType()));
                }
            } else {
                bindings.add(new ParamBinding(param.getName(), param, param.getType()));
            }
        }
        return bindings;
    }

    /**
     * Bindings available at the given context element, including item/index variables
     * declared by enclosing foreach tags between the element and the statement tag.
     */
    @NotNull
    public static List<ParamBinding> resolveBindingsAt(@Nullable PsiElement context) {
        XmlTag statementTag = enclosingStatementTag(context);
        if (statementTag == null) return Collections.emptyList();
        List<ParamBinding> bindings = new ArrayList<>(resolveBindings(statementTag));

        PsiElement current = context;
        while (current != null && current != statementTag) {
            if (current instanceof XmlTag && "foreach".equals(((XmlTag) current).getName())) {
                XmlTag foreach = (XmlTag) current;
                addForeachVar(bindings, foreach, "item");
                addForeachVar(bindings, foreach, "index");
            }
            current = current.getParent();
        }
        return bindings;
    }

    private static void addForeachVar(List<ParamBinding> bindings, XmlTag foreach, String attrName) {
        XmlAttribute attr = foreach.getAttribute(attrName);
        if (attr == null) return;
        XmlAttributeValue valueElement = attr.getValueElement();
        String value = attr.getValue();
        if (valueElement == null || value == null || value.isEmpty()) return;
        bindings.add(new ParamBinding(value, valueElement, null));
    }

    /**
     * Returns the first segment of a parameter expression: trims, then cuts at the
     * first '.', ',', '[' or whitespace.
     */
    @NotNull
    public static String firstSegment(@Nullable String expression) {
        if (expression == null) return "";
        String trimmed = expression.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '.' || c == ',' || c == '[' || Character.isWhitespace(c)) {
                return trimmed.substring(0, i);
            }
        }
        return trimmed;
    }

    /**
     * True for primitives, boxed types, String, java.util.Date, java.time.*, BigDecimal/BigInteger.
     */
    public static boolean isSimpleType(@Nullable PsiType type) {
        if (type == null) return false;
        if (type instanceof PsiPrimitiveType) return true;
        if (type instanceof PsiClassType) {
            String fqn = canonicalNameNoGenerics(type);
            if (fqn == null) return false;
            return SIMPLE_TYPE_FQNS.contains(fqn) || fqn.startsWith("java.time.");
        }
        return false;
    }

    @Nullable
    private static String canonicalNameNoGenerics(PsiType type) {
        PsiClass cls = resolveClass(type);
        return cls != null ? cls.getQualifiedName() : null;
    }

    @Nullable
    private static PsiClass resolveClass(PsiType type) {
        return type instanceof PsiClassType ? ((PsiClassType) type).resolve() : null;
    }

    @Nullable
    private static PsiAnnotation findParamAnnotation(PsiParameter param) {
        return param.getAnnotation(PARAM_ANNOTATION);
    }

    @NotNull
    private static List<PsiParameter> effectiveParameters(PsiMethod method) {
        List<PsiParameter> result = new ArrayList<>();
        for (PsiParameter param : method.getParameterList().getParameters()) {
            PsiClass cls = resolveClass(param.getType());
            String fqn = cls != null ? cls.getQualifiedName() : null;
            if (fqn != null && IGNORED_PARAM_TYPES.contains(fqn)) continue;
            result.add(param);
        }
        return result;
    }
}
