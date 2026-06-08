/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.util;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ide.highlighter.XmlFileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapperXmlIndex {

    public static final List<String> STATEMENT_TAG_NAMES =
            Collections.unmodifiableList(Arrays.asList("select", "insert", "update", "delete"));
    public static final List<String> REFERENCEABLE_TAG_NAMES =
            Collections.unmodifiableList(Arrays.asList("select", "insert", "update", "delete", "resultMap", "sql"));

    private MapperXmlIndex() {}

    public static List<XmlFile> findXmlMapperFiles(Project project, String interfaceFqn) {
        if (interfaceFqn == null || interfaceFqn.isEmpty()) return Collections.emptyList();
        Map<String, List<VirtualFile>> map = getOrBuildNamespaceMap(project);
        List<VirtualFile> vfs = map.get(interfaceFqn);
        if (vfs == null || vfs.isEmpty()) return Collections.emptyList();
        PsiManager pm = PsiManager.getInstance(project);
        List<XmlFile> result = new ArrayList<>(vfs.size());
        for (VirtualFile vf : vfs) {
            if (!vf.isValid()) continue;
            PsiFile psi = pm.findFile(vf);
            if (psi instanceof XmlFile && psi.isValid()) result.add((XmlFile) psi);
        }
        return result;
    }

    public static XmlTag findXmlStatement(XmlFile xmlFile, String methodName) {
        if (xmlFile == null || methodName == null) return null;
        XmlTag root = xmlFile.getRootTag();
        if (root == null || !"mapper".equals(root.getName())) return null;
        for (XmlTag child : root.getSubTags()) {
            if (STATEMENT_TAG_NAMES.contains(child.getName())
                    && methodName.equals(child.getAttributeValue("id"))) {
                return child;
            }
        }
        return null;
    }

    public static PsiClass findMapperInterface(Project project, String namespace) {
        if (namespace == null || namespace.isEmpty()) return null;
        if (DumbService.isDumb(project)) return null;
        PsiClass cls = JavaPsiFacade.getInstance(project).findClass(namespace, GlobalSearchScope.projectScope(project));
        return cls != null && cls.isInterface() ? cls : null;
    }

    public static PsiMethod findMapperMethod(PsiClass mapperInterface, String id) {
        if (mapperInterface == null || id == null) return null;
        PsiMethod[] candidates = mapperInterface.findMethodsByName(id, false);
        return candidates.length > 0 ? candidates[0] : null;
    }

    public static String readNamespace(XmlFile xmlFile) {
        if (xmlFile == null) return null;
        XmlTag root = xmlFile.getRootTag();
        if (root == null || !"mapper".equals(root.getName())) return null;
        return root.getAttributeValue("namespace");
    }

    private static Map<String, List<VirtualFile>> getOrBuildNamespaceMap(Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            if (DumbService.isDumb(project)) {
                return CachedValueProvider.Result.create(Collections.emptyMap(),
                        PsiModificationTracker.MODIFICATION_COUNT, DumbService.getInstance(project).getModificationTracker());
            }
            Map<String, List<VirtualFile>> map = new HashMap<>();
            Collection<VirtualFile> files = FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
            PsiManager psiManager = PsiManager.getInstance(project);
            for (VirtualFile vf : files) {
                PsiFile psi = psiManager.findFile(vf);
                if (!(psi instanceof XmlFile)) continue;
                String ns = readNamespace((XmlFile) psi);
                if (ns == null || ns.isEmpty()) continue;
                map.computeIfAbsent(ns, k -> new ArrayList<>()).add(vf);
            }
            return CachedValueProvider.Result.create(map, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }
}
