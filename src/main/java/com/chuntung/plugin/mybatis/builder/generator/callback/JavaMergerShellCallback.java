/*
 * Copyright (c) 2019-2021 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.generator.callback;

import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom shell callback for merging java via regrex.
 *
 * @author Tony Ho
 */
public class JavaMergerShellCallback extends DefaultShellCallback {

    private static final String JAVA_PREFIX = "java";
    private static final String NEWLINE = System.getProperty("line.separator", "\n");

    // generated code    : /\*\*\n(\s*\*.*\n)*\s*\* @mbg.generated.*\n\s*\*/\n((?!\s*\n)[\s\S])+\n

    // comment start     : /\*\*\n
    // multiple comments : (\s*\*.*\n)*
    // special comment   : \s*\* @mbg.generated.*\n
    // comment end       : \s*\*/\n
    // method            : ((?!\s*\n)[\s\S])+\n
    // space line        : \s*\n
    private static final String GENERATED = "\\s*/\\*\\*\\n(\\s*\\*.*\\n)*\\s*\\* "
            + MergeConstants.NEW_ELEMENT_TAG + ".*\\n\\s*\\*/\\n((?!\\s*\\n)[\\s\\S])+\\n";
    private static final String GENERATED_WITH_LINE = GENERATED + "\\s*\\n";

    private static final Pattern BODY_PATTERN = Pattern.compile("public interface [^\\{]*\\{([\\s\\S]+)\\}");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import (.*);\\r?\\n");

    static class Import {
        boolean isStatic;
        boolean isAsterisk;
        String name;

        Import(String text) {
            this.isAsterisk = text.endsWith("*");
            this.isStatic = text.startsWith("static");
            this.name = text;
            if (this.isStatic) {
                this.name = this.name.substring("static".length());
            }
            if (this.isAsterisk) {
                this.name = this.name.substring(0, this.name.lastIndexOf('.'));
            }
        }

        StringBuffer asString(Import former) {
            StringBuffer sb = new StringBuffer();
            if (former != null && (former.isStatic != isStatic
                    || former.name.startsWith(JAVA_PREFIX) != name.startsWith(JAVA_PREFIX))) {
                // separate by if static or if java
                sb.append(NEWLINE);
            }
            sb.append("import").append(isStatic ? " static" : " ")
                    .append(name).append(isAsterisk ? ".*;" : ";");
            return sb;
        }

    }

    /**
     * Instantiates a new default shell callback.
     *
     * @param overwrite the overwrite
     */
    public JavaMergerShellCallback(boolean overwrite) {
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
        if (!BODY_PATTERN.matcher(newFileSource).find()) {
            return newFileSource;
        }

        try {
            String oldSrc = StringUtil.readFromFile(existingFile, Charset.forName(fileEncoding));
            if (oldSrc.equals(newFileSource) || !includingCustom(oldSrc)) {
                return newFileSource;
            }

            List<Import> imports = new ArrayList<>();
            StringBuffer sb = new StringBuffer();
            Matcher newImportMatcher = IMPORT_PATTERN.matcher(newFileSource);
            // append chars before import
            while (newImportMatcher.find()) {
                // extract new imports
                newImportMatcher.appendReplacement(sb, "");
                imports.add(new Import(newImportMatcher.group(1)));
            }

            // trim tail
            while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                sb.setLength(sb.length() - 1);
            }
            sb.append(NEWLINE);

            // append merged imports
            Matcher oldImportMatcher = IMPORT_PATTERN.matcher(oldSrc);
            while (oldImportMatcher.find()) {
                imports.add(new Import(oldImportMatcher.group(1)));
            }

            mergeImport(imports, sb);

            // append chars after import
            newImportMatcher.appendTail(sb);

            // remove the end char
            sb.setLength(sb.lastIndexOf("}"));

            // append delta body
            String deltaSrc = oldSrc.replace("\r", "")
                    .replaceAll(GENERATED_WITH_LINE, "\n")
                    .replaceAll(GENERATED, "\n")
                    // merge new line and change to system new line format
                    .replaceAll("\\n\\n+", "\n\n")
                    .replaceAll("\\n", NEWLINE);
            Matcher bodyMatcher = BODY_PATTERN.matcher(deltaSrc);
            if (bodyMatcher.find()) {
                String stmt = bodyMatcher.group(1);
                sb.append(stmt);
                if (!stmt.endsWith(NEWLINE)) {
                    sb.append(NEWLINE);
                }
            }

            // append end char
            sb.append("}");

            return sb.toString();
        } catch (IOException e) {
            throw new ShellException(e.getMessage());
        }
    }

    private void mergeImport(List<Import> imports, StringBuffer sb) {
        // idea import order: custom -> java -> static
        imports.sort(Comparator.comparingInt((Import l) -> (l.isStatic ? 1 : 0))
                .thenComparingInt(l -> (l.name.startsWith("java") ? 1 : 0))
                .thenComparing(l -> l.name));

        Iterator<Import> iterator = imports.iterator();
        if (iterator.hasNext()) {
            sb.append(NEWLINE);

            Import former = iterator.next();
            sb.append(former.asString(null)).append(NEWLINE);
            while (iterator.hasNext()) {
                Import current = iterator.next();
                if (current.isStatic == former.isStatic
                        && (current.name.equals(former.name)
                        || former.isAsterisk && (current.name.startsWith(former.name)))) {
                    // merge into former asterisk import or same import
                    iterator.remove();
                } else {
                    sb.append(current.asString(former)).append(NEWLINE);
                    former = current;
                }
            }
        }
    }

    private boolean includingCustom(String oldSrc) {
        ParseResult<CompilationUnit> result = new JavaParser().parse(oldSrc);
        if (!result.isSuccessful()) {
            return true;
        }

        CompilationUnit compilationUnit = result.getResult().get();
        return compilationUnit.getType(0).getMembers().stream()
                .anyMatch(x -> !x.getComment().isPresent()
                        || !x.getComment().get().getContent().contains(MergeConstants.NEW_ELEMENT_TAG));
    }

}