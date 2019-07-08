/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.callback;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Custom shell callback to support auto merge.
 *
 * @author Tony Ho
 */
public class CustomShellCallback extends DefaultShellCallback {

    private static final String JAVA_PREFIX = "java.";
    private static final String NEWLINE = System.getProperty("line.separator", "\n");

    /**
     * Instantiates a new default shell callback.
     *
     * @param overwrite the overwrite
     */
    public CustomShellCallback(boolean overwrite) {
        super(overwrite);
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

    @Override
    public String mergeJavaFile(String newFileSource, File existingFile, String[] javadocTags, String fileEncoding)
            throws ShellException {
        // only support interface file
        if (!newFileSource.contains("public interface")) {
            return newFileSource;
        }

        ParseResult<CompilationUnit> newParseResult = new JavaParser().parse(newFileSource);
        ParseResult<CompilationUnit> oldParseResult = null;
        try {
            oldParseResult = new JavaParser().parse(existingFile);
            if (oldParseResult.getProblems().size() > 0) {
                List<String> problems = new ArrayList<>(oldParseResult.getProblems().size());
                for (Problem problem : oldParseResult.getProblems()) {
                    problems.add(problem.getMessage());
                }
                StringBuilder sb = new StringBuilder("Failed to parse ").append(existingFile.getCanonicalPath());
                sb.append("\nProblems:\n").append(String.join("\n", problems));
                throw new ShellException(sb.toString());
            }
        } catch (IOException e) {
            throw new ShellException(e.getMessage());
        }

        return mergerFile(newParseResult.getResult().get(), oldParseResult.getResult().get());
    }

    public String mergerFile(CompilationUnit newCompilationUnit, CompilationUnit oldCompilationUnit) {
        List<MethodDeclaration> oldMethods = oldCompilationUnit.getType(0).getMethods();
        TypeDeclaration<?> typeDeclaration = newCompilationUnit.getType(0);
        List<MethodDeclaration> newMethods = typeDeclaration.getMethods();

        NodeList<ImportDeclaration> oldImports = oldCompilationUnit.getImports();
        NodeList<ImportDeclaration> newImports = newCompilationUnit.getImports();

        LinkedHashSet<ImportDeclaration> usedImports = new LinkedHashSet<>();
        LinkedHashSet<ImportDeclaration> deltaImports = new LinkedHashSet<>(oldImports);
        deltaImports.removeAll(newImports);

        LinkedHashSet<MethodDeclaration> deltaMethods = new LinkedHashSet<>(oldMethods);
        deltaMethods.removeAll(newMethods);
        for (MethodDeclaration methodDeclaration : deltaMethods) {
            if (methodDeclaration.getComment().isPresent()) {
                if (methodDeclaration.getComment().get().getContent().contains(MergeConstants.NEW_ELEMENT_TAG)) {
                    // skip generated methods
                    continue;
                }
            }

            findOutUsedImport(methodDeclaration, deltaImports, usedImports);

            typeDeclaration.addMember(methodDeclaration);
        }

        // add custom used imports
        if (!usedImports.isEmpty()) {
            for (ImportDeclaration importDeclaration : usedImports) {
                newCompilationUnit.addImport(importDeclaration);
            }

            // re-sort by name, jdk type at the end
            newCompilationUnit.getImports().sort(new Comparator<ImportDeclaration>() {
                @Override
                public int compare(ImportDeclaration o1, ImportDeclaration o2) {
                    String left = o1.getNameAsString();
                    String right = o2.getNameAsString();
                    if (left.startsWith(JAVA_PREFIX) && !right.startsWith(JAVA_PREFIX)) {
                        return 1;
                    } else if (!left.startsWith(JAVA_PREFIX) && right.startsWith(JAVA_PREFIX)) {
                        return -1;
                    } else {
                        return left.compareTo(right);
                    }
                }
            });
        }

        String sourceCode = newCompilationUnit.toString();
        if (sourceCode.endsWith(NEWLINE)) {
            sourceCode = sourceCode.substring(0, sourceCode.length() - NEWLINE.length());
        }
        return sourceCode;
    }

    // check the method to find out used imports from delta imports
    private void findOutUsedImport(MethodDeclaration declaration, Set<ImportDeclaration> deltaImports, Set<ImportDeclaration> usedImports) {
        Iterator<ImportDeclaration> iterator = deltaImports.iterator();
        while (iterator.hasNext()) {
            ImportDeclaration importDeclaration = iterator.next();
            if (existsImport(importDeclaration, declaration)) {
                iterator.remove();
                usedImports.add(importDeclaration);
            }
        }
    }

    // check if the import is used for the method
    private boolean existsImport(ImportDeclaration importDeclaration, MethodDeclaration methodDeclaration) {
        String type = importDeclaration.getNameAsString();
        String simpleType = type.substring(type.lastIndexOf('.') + 1);

        Type returnType = methodDeclaration.getType();
        if (typeIsImported(returnType, simpleType)) {
            return true;
        }

        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        for (Parameter parameter : parameters) {
            if (typeIsImported(parameter.getType(), simpleType)) {
                return true;
            }

            NodeList<AnnotationExpr> annotations = parameter.getAnnotations();
            for (AnnotationExpr annotation : annotations) {
                if (annotation.getNameAsString().equals(simpleType)) {
                    return true;
                }
            }
        }

        return false;
    }

    // check if the type or type arguments use the imported type
    private boolean typeIsImported(Type type, String simpleTypeName) {
        // ClassOrInterfaceType
        if (type instanceof ClassOrInterfaceType) {
            ClassOrInterfaceType concreteType = (ClassOrInterfaceType) type;
            // type
            if (concreteType.getName().asString().equals(simpleTypeName)) {
                return true;
            }

            // type argument
            if (concreteType.getTypeArguments().isPresent()) {
                for (Type argType : concreteType.getTypeArguments().get()) {
                    if (argType instanceof ClassOrInterfaceType) {
                        ClassOrInterfaceType concreteArgType = (ClassOrInterfaceType) argType;
                        if (concreteArgType.getName().asString().equals(simpleTypeName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
