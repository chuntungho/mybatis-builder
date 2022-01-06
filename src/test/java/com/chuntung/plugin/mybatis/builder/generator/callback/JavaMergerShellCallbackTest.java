package com.chuntung.plugin.mybatis.builder.generator.callback;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mybatis.generator.exception.ShellException;

import java.io.File;
import java.io.IOException;

public class JavaMergerShellCallbackTest extends TestCase {

    @Test
    public void testMergeJavaFile() throws IOException, ShellException {
        JavaMergerShellCallback callback = new JavaMergerShellCallback(true);
        String newFileSource = FileUtils.readFileToString(new File("src/test/java/com/chuntung/plugin/mybatis/builder/mapper/DemoNewMapper.java"));
        File existingFile = new File("src/test/java/com/chuntung/plugin/mybatis/builder/mapper/DemoMapper.java");
        String mergedFileSource = callback.mergeJavaFile(newFileSource, existingFile, null, "utf-8");

        System.out.println(mergedFileSource);
        ParseResult<CompilationUnit> result = new JavaParser().parse(mergedFileSource);
        assertTrue(result.isSuccessful());
    }
}