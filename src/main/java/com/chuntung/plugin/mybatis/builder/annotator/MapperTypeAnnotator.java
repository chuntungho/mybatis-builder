/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.annotator;

import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapperTypeAnnotator implements Annotator {

    private static final List<String> COLLECTION_FQNS = Arrays.asList(
            "java.util.List", "java.util.Collection", "java.lang.Iterable", "java.util.Set");

    // MyBatis built-in scalar type aliases (org.apache.ibatis.type.TypeAliasRegistry).
    // Keys are lowercase; MyBatis matches aliases case-insensitively.
    private static final Map<String, String> ALIAS_TO_FQN = buildAliasMap();

    private static Map<String, String> buildAliasMap() {
        Map<String, String> m = new HashMap<>();
        m.put("string", "java.lang.String");
        m.put("byte", "java.lang.Byte");
        m.put("char", "java.lang.Character");
        m.put("character", "java.lang.Character");
        m.put("long", "java.lang.Long");
        m.put("short", "java.lang.Short");
        m.put("int", "java.lang.Integer");
        m.put("integer", "java.lang.Integer");
        m.put("double", "java.lang.Double");
        m.put("float", "java.lang.Float");
        m.put("boolean", "java.lang.Boolean");
        // Underscore-prefixed aliases are primitives in MyBatis; box them for class-based comparison.
        m.put("_byte", "java.lang.Byte");
        m.put("_char", "java.lang.Character");
        m.put("_character", "java.lang.Character");
        m.put("_long", "java.lang.Long");
        m.put("_short", "java.lang.Short");
        m.put("_int", "java.lang.Integer");
        m.put("_integer", "java.lang.Integer");
        m.put("_double", "java.lang.Double");
        m.put("_float", "java.lang.Float");
        m.put("_boolean", "java.lang.Boolean");
        m.put("date", "java.util.Date");
        m.put("decimal", "java.math.BigDecimal");
        m.put("bigdecimal", "java.math.BigDecimal");
        m.put("biginteger", "java.math.BigInteger");
        m.put("object", "java.lang.Object");
        m.put("map", "java.util.Map");
        m.put("hashmap", "java.util.HashMap");
        m.put("list", "java.util.List");
        m.put("arraylist", "java.util.ArrayList");
        m.put("collection", "java.util.Collection");
        m.put("iterator", "java.util.Iterator");
        return m;
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (DumbService.isDumb(element.getProject())) return;
        if (!(element instanceof XmlAttributeValue)) return;
        XmlAttributeValue value = (XmlAttributeValue) element;
        if (!(value.getParent() instanceof XmlAttribute)) return;
        XmlAttribute attr = (XmlAttribute) value.getParent();
        String attrName = attr.getName();
        if (!"parameterType".equals(attrName) && !"resultType".equals(attrName)) return;

        XmlTag statementTag = attr.getParent();
        if (statementTag == null) return;
        if (!MapperXmlIndex.STATEMENT_TAG_NAMES.contains(statementTag.getName())) return;
        XmlTag mapperTag = statementTag.getParentTag();
        if (mapperTag == null || !"mapper".equals(mapperTag.getName())) return;

        if ("resultType".equals(attrName) && statementTag.getAttribute("resultMap") != null) return;

        String declaredFqn = value.getValue();
        if (declaredFqn == null || declaredFqn.isEmpty()) return;

        if (!(value.getContainingFile() instanceof XmlFile)) return;
        XmlFile xmlFile = (XmlFile) value.getContainingFile();
        String namespace = MapperXmlIndex.readNamespace(xmlFile);
        if (namespace == null) return;

        PsiClass mapperInterface = MapperXmlIndex.findMapperInterface(value.getProject(), namespace);
        if (mapperInterface == null) return;

        String id = statementTag.getAttributeValue("id");
        if (id == null) return;
        PsiMethod method = MapperXmlIndex.findMapperMethod(mapperInterface, id);
        if (method == null) return;

        String resolvedFqn = ALIAS_TO_FQN.getOrDefault(declaredFqn.toLowerCase(), declaredFqn);
        PsiClass declaredClass = JavaPsiFacade.getInstance(value.getProject())
                .findClass(resolvedFqn, GlobalSearchScope.allScope(value.getProject()));
        if (declaredClass == null) return;

        PsiType expected = "parameterType".equals(attrName)
                ? expectedParameterType(method)
                : expectedResultType(method);
        if (expected == null) return;

        if (typesCompatible(expected, declaredClass, mapperInterface)) return;

        String message = "parameterType".equals(attrName)
                ? "parameterType '" + declaredFqn + "' is unrelated to method parameter type '"
                        + expected.getCanonicalText() + "'"
                : "resultType '" + declaredFqn + "' is unrelated to method return type '"
                        + expected.getCanonicalText() + "'";

        holder.newAnnotation(HighlightSeverity.WARNING, message)
                .range(value.getTextRange())
                .create();
    }

    @Nullable
    private static PsiType expectedParameterType(PsiMethod method) {
        PsiParameter[] params = method.getParameterList().getParameters();
        if (params.length != 1) return null;
        return params[0].getType();
    }

    @Nullable
    private static PsiType expectedResultType(PsiMethod method) {
        PsiType returnType = method.getReturnType();
        if (returnType == null || PsiType.VOID.equals(returnType)) return null;
        if (returnType instanceof PsiArrayType) {
            return ((PsiArrayType) returnType).getComponentType();
        }
        if (returnType instanceof PsiClassType) {
            PsiClassType ct = (PsiClassType) returnType;
            PsiClass resolved = ct.resolve();
            if (resolved != null) {
                String fqn = resolved.getQualifiedName();
                if (COLLECTION_FQNS.contains(fqn)) {
                    PsiType[] args = ct.getParameters();
                    if (args.length == 1) return args[0];
                }
            }
        }
        return returnType;
    }

    private static boolean typesCompatible(PsiType expected, PsiClass declared, PsiElement context) {
        String declaredFqn = declared.getQualifiedName();
        if ("java.lang.Object".equals(declaredFqn)) return true;
        if (declaredFqn != null && declaredFqn.startsWith("java.util.") && declaredFqn.contains("Map")) {
            return true;
        }

        PsiType normalizedExpected = boxIfPrimitive(expected, context);
        if (normalizedExpected == null) return true;

        PsiClassType declaredType = JavaPsiFacade.getElementFactory(context.getProject())
                .createType(declared);

        if (normalizedExpected.equals(declaredType)) return true;
        if (normalizedExpected.isAssignableFrom(declaredType)) return true;
        if (declaredType.isAssignableFrom(normalizedExpected)) return true;
        return false;
    }

    @Nullable
    private static PsiType boxIfPrimitive(PsiType type, PsiElement context) {
        if (type instanceof PsiPrimitiveType) {
            return ((PsiPrimitiveType) type).getBoxedType(context);
        }
        return type;
    }
}
