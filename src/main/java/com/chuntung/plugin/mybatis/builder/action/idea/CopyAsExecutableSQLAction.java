/*
 * Copyright (c) 2019 Tony Ho. Some rights reserved.
 */

package com.chuntung.plugin.mybatis.builder.action.idea;

import com.chuntung.plugin.mybatis.builder.util.StringUtil;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.*;

/**
 * Resolve placeholders of printed SQL in MyBatis log, copy as executable SQL to clipboard.<br>
 * The selected text should contains two keywords: "{@code Preparing: }", "{@code Parameters: }"
 *
 * @author Tony Ho
 */
public class CopyAsExecutableSQLAction extends DumbAwareAction {

    private static final String PREPARING = "Preparing: ";
    private static final String PARAMETERS = "Parameters: ";
    private static final Set<String> QUOTE_TYPES = new HashSet<>(Arrays.asList("(String)", "(Date)", "(Time)", "(Timestamp)", "(DateTime)", "(LocalDateTime)"));

    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        if (selectedText == null || !selectedText.contains(PREPARING) && !selectedText.contains(PARAMETERS)) {
            String error = "Keywords \"" + PREPARING + "\" and \"" + PARAMETERS + "\" are required";
            HintManager.getInstance().showErrorHint(editor, error);
            return;
        }

        String sql = resolve(selectedText);

        // copy sql to clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sql), null);

        NotificationHelper.getInstance().notifyInfo("Executable SQL copied to clipboard", event.getProject());
    }

    @NotNull
    public static String resolve(String selectedText) {
        // support multiple sql
        int fromIndex = 0, nextIndex;
        List<String> sqlList = new ArrayList<>();
        while ((nextIndex = selectedText.indexOf(PREPARING, fromIndex)) >= 0) {
            String statement = extractLine(selectedText, PREPARING, fromIndex);
            if (selectedText.indexOf(PARAMETERS, nextIndex) == -1) {
                // has no parameters for current statement, return the statement
                sqlList.add(statement);
                break;
            }
            String parametersText = extractLine(selectedText, PARAMETERS, nextIndex);
            List<String> parameters = parseParameters(parametersText);

            StringTokenizer tokenizer = new StringTokenizer(statement, "?");
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (tokenizer.hasMoreTokens()) {
                sb.append(tokenizer.nextToken());
                if (i < parameters.size()) {
                    sb.append(parameters.get(i));
                }
                i++;
            }

            sqlList.add(sb.toString().trim());

            fromIndex = nextIndex + PREPARING.length();
        }

        if (sqlList.size() == 0) {
            return "";
        } else if (sqlList.size() == 1) {
            return sqlList.get(0);
        } else {
            return String.join(";" + System.lineSeparator(), sqlList) + ";";
        }
    }

    private static List<String> parseParameters(String parametersText) {
        List<String> parameters = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(parametersText, ",");
        while (tokenizer.hasMoreTokens()) {
            String paramText = tokenizer.nextToken();
            int idx = paramText.lastIndexOf("(");
            // null parameter has no type
            if (idx == -1) {
                idx = paramText.length();
            }

            String paramType = paramText.substring(idx);
            String paramValue = paramText.substring(0, idx).trim();
            if (QUOTE_TYPES.contains(paramType)) {
                paramValue = "'" + paramValue + "'";
            }
            parameters.add(paramValue);
        }
        return parameters;
    }

    private static String extractLine(String str, String keyword, int fromIndex) {
        int start = str.indexOf(keyword, fromIndex) + keyword.length();
        int end = str.indexOf('\n', start);

        return str.substring(start, end > -1 ? end : str.length());
    }

    // only visible for selection
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();
            if (StringUtil.stringHasValue(selectedText)) {
                return;
            }
        }

        event.getPresentation().setVisible(false);
    }
}
