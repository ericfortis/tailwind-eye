package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditStringAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) return;

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) return;

        // Try to find a string literal or similar
        // For simplicity, let's look for a PsiElement that contains quotes or is a string literal
        // Since we don't know the exact PSI structure for all languages, 
        // we can look at the text around the caret for quotes.

        TextRange foundRange = findStringRange(editor.getDocument(), offset);
        if (foundRange == null) return;

        String originalContent = editor.getDocument().getText(foundRange);
        List<String> classes = Arrays.stream(originalContent.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        JBList<String> list = new JBList<>(classes);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                return label;
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(300, Math.min(600, Math.max(200, classes.size() * 25 + 10))));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(scrollPane, list)
//                .setFocusable(true)
//                .setRequestFocus(true)
                .setResizable(true)
                .setMovable(true)
                .createPopup();

        popup.showInBestPositionFor(editor);
    }

    private TextRange findStringRange(Document document, int offset) {
        String text = document.getText();
        int start = -1;
        int end = -1;

        // Search backward for " or '
        for (int i = offset; i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                start = i + 1;
                break;
            }
        }

        // Search forward for " or '
        for (int i = offset; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                end = i;
                break;
            }
        }

        if (start != -1 && end != -1 && start <= end) {
            return new TextRange(start, end);
        }
        return null;
    }
}
