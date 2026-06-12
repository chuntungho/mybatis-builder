/*
 * Copyright (c) 2026 Chuntung Ho. Some rights reserved.
 */
package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.MybatisBuilderBundle;
import com.chuntung.plugin.mybatis.builder.util.MapperXmlIndex;
import com.chuntung.plugin.mybatis.builder.util.StatementTypeInferrer;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiType;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class GenerateXmlStatementIntention implements IntentionAction {

    private static final String[] STATEMENT_TYPES = {"select", "insert", "update", "delete"};

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getText() {
        return MybatisBuilderBundle.message("action.generate.xml.text");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return MybatisBuilderBundle.message("action.intention.family");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        PsiMethod method = findMethodAtCaret(editor, file);
        if (method == null) return false;
        PsiClass cls = method.getContainingClass();
        if (cls == null || !cls.isInterface() || cls.getQualifiedName() == null) return false;
        List<XmlFile> mappers = MapperXmlIndex.findXmlMapperFiles(project, cls.getQualifiedName());
        if (mappers.isEmpty()) {
            return looksLikeMapperInterface(cls);
        }
        for (XmlFile xml : mappers) {
            if (MapperXmlIndex.findXmlStatement(xml, method.getName()) != null) return false;
        }
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        PsiMethod method = findMethodAtCaret(editor, file);
        if (method == null) return;
        PsiClass cls = method.getContainingClass();
        if (cls == null || cls.getQualifiedName() == null) return;
        List<XmlFile> mappers = MapperXmlIndex.findXmlMapperFiles(project, cls.getQualifiedName());

        if (mappers.isEmpty()) {
            XmlFile created = createXmlMapperFile(project, cls);
            if (created == null) return;
            mappers = java.util.Collections.singletonList(created);
        }

        String defaultTag = StatementTypeInferrer.inferTagName(method.getName());
        StubChoiceDialog dialog = new StubChoiceDialog(project, defaultTag, mappers);
        if (!dialog.showAndGet()) return;

        XmlFile targetFile = dialog.getTargetFile();
        String tagName = dialog.getTagName();

        WriteCommandAction.runWriteCommandAction(project, "Generate MyBatis XML Statement", null, () -> {
            XmlTag root = targetFile.getRootTag();
            if (root == null) return;
            XmlTag newTag = buildStatementTag(project, tagName, method);
            XmlTag added = root.addSubTag(newTag, false);
            if (added instanceof Navigatable) {
                ((Navigatable) added).navigate(true);
            }
        }, targetFile);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Nullable
    private static PsiMethod findMethodAtCaret(Editor editor, PsiFile file) {
        if (editor == null || file == null) return null;
        PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());
        if (!(at instanceof PsiIdentifier)) return null;
        PsiElement parent = at.getParent();
        return parent instanceof PsiMethod ? (PsiMethod) parent : null;
    }

    private static XmlTag buildStatementTag(Project project, String tagName, PsiMethod method) {
        String resultType = "select".equals(tagName) ? extractResultType(method) : null;
        StringBuilder sb = new StringBuilder();
        sb.append('<').append(tagName).append(" id=\"").append(method.getName()).append('"');
        if (resultType != null) {
            sb.append(" resultType=\"").append(resultType).append('"');
        }
        sb.append(">\n");
        sb.append("    <!-- TODO: ").append(method.getName()).append(" -->\n");
        sb.append("</").append(tagName).append('>');
        return XmlElementFactory.getInstance(project).createTagFromText(sb.toString());
    }

    @Nullable
    private static String extractResultType(PsiMethod method) {
        PsiType returnType = method.getReturnType();
        if (returnType == null || PsiType.VOID.equals(returnType)) return null;
        if (returnType instanceof PsiArrayType) {
            return canonical(((PsiArrayType) returnType).getComponentType());
        }
        if (returnType instanceof PsiClassType) {
            PsiClassType ct = (PsiClassType) returnType;
            PsiClass resolved = ct.resolve();
            if (resolved != null) {
                String fqn = resolved.getQualifiedName();
                if ("java.util.List".equals(fqn) || "java.util.Collection".equals(fqn)
                        || "java.lang.Iterable".equals(fqn) || "java.util.Set".equals(fqn)) {
                    PsiType[] params = ct.getParameters();
                    if (params.length == 1) return canonical(params[0]);
                }
            }
            return canonical(returnType);
        }
        return canonical(returnType);
    }

    private static String canonical(PsiType type) {
        return type != null ? type.getCanonicalText() : null;
    }

    private static boolean looksLikeMapperInterface(PsiClass cls) {
        PsiModifierList modifiers = cls.getModifierList();
        if (modifiers == null) return false;
        for (PsiAnnotation a : modifiers.getAnnotations()) {
            String qn = a.getQualifiedName();
            if (qn == null) continue;
            if (qn.endsWith(".Mapper") || qn.endsWith(".Repository")) return true;
        }
        return false;
    }

    private static XmlFile createXmlMapperFile(Project project, PsiClass cls) {
        VirtualFile baseDir = guessDefaultResourcesDir(project, cls);
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle("Select Directory for New MyBatis XML Mapper")
                .withDescription("The file " + cls.getName() + ".xml will be created here");
        VirtualFile dir = FileChooser.chooseFile(descriptor, project, baseDir);
        if (dir == null) return null;

        String fileName = cls.getName() + ".xml";
        if (dir.findChild(fileName) != null) {
            Messages.showErrorDialog(project, fileName + " already exists in the selected directory.", "Cannot Create File");
            return null;
        }

        FileTemplate tpl = FileTemplateManager.getInstance(project).getInternalTemplate("MyBatis Mapper.xml");
        Properties props = new Properties();
        props.setProperty("NAMESPACE", cls.getQualifiedName());

        try {
            String text = tpl.getText(props);
            PsiFile psi = WriteAction.compute(() -> {
                VirtualFile f = dir.createChildData(GenerateXmlStatementIntention.class, fileName);
                VfsUtil.saveText(f, text);
                return PsiManager.getInstance(project).findFile(f);
            });
            return psi instanceof XmlFile ? (XmlFile) psi : null;
        } catch (IOException e) {
            Messages.showErrorDialog(project, e.getMessage(), "Cannot Create File");
            return null;
        }
    }

    @Nullable
    private static VirtualFile guessDefaultResourcesDir(Project project, PsiClass cls) {
        Module module = ModuleUtilCore.findModuleForPsiElement(cls);
        if (module != null) {
            for (VirtualFile root : ModuleRootManager.getInstance(module).getSourceRoots(false)) {
                if (!root.getPath().contains("resources")) continue;
                VirtualFile mapper = root.findChild("mapper");
                if (mapper != null && mapper.isDirectory()) return mapper;
                return root;
            }
        }
        return ProjectUtil.guessProjectDir(project);
    }

    private static class StubChoiceDialog extends DialogWrapper {
        private final String defaultTag;
        private final List<XmlFile> targets;
        private final JComboBox<String> tagBox = new JComboBox<>(STATEMENT_TYPES);
        private final JComboBox<XmlFile> targetBox;

        StubChoiceDialog(Project project, String defaultTag, List<XmlFile> targets) {
            super(project);
            this.defaultTag = defaultTag;
            this.targets = targets;
            this.targetBox = new JComboBox<>(targets.toArray(new XmlFile[0]));
            setTitle("Generate MyBatis XML Statement");
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new GridLayout(0, 2, 8, 4));
            tagBox.setSelectedItem(defaultTag);
            panel.add(new JLabel("Statement type:"));
            panel.add(tagBox);
            panel.add(new JLabel("Target XML file:"));
            panel.add(targetBox);
            if (targets.size() == 1) {
                targetBox.setEnabled(false);
            }
            return panel;
        }

        String getTagName() {
            return (String) tagBox.getSelectedItem();
        }

        XmlFile getTargetFile() {
            return (XmlFile) targetBox.getSelectedItem();
        }
    }
}
