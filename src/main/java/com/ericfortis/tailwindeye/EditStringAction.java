package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
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

        // Use a RangeMarker to track the range as the document is edited
        com.intellij.openapi.editor.RangeMarker rangeMarker = editor.getDocument().createRangeMarker(foundRange);

        String originalContent = editor.getDocument().getText(foundRange);
        String popupContent = Arrays.stream(originalContent.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));

        Document tempDocument = EditorFactory.getInstance().createDocument(popupContent);
        EditorFactory editorFactory = EditorFactory.getInstance();
        EditorEx popupEditor = (EditorEx) editorFactory.createEditor(tempDocument, project, FileTypeManager.getInstance().getFileTypeByExtension("txt"), false);

        popupEditor.getSettings().setFoldingOutlineShown(false);
        popupEditor.getComponent().setPreferredSize(new Dimension(400, 300));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupEditor.getComponent(), popupEditor.getContentComponent())
                .setFocusable(true)
                .setRequestFocus(true)
                .setResizable(true)
                .setMovable(true)
                .addListener(new com.intellij.openapi.ui.popup.JBPopupListener() {
                    @Override
                    public void onClosed(@NotNull com.intellij.openapi.ui.popup.LightweightWindowEvent event) {
                        if (!popupEditor.isDisposed()) {
                            editorFactory.releaseEditor(popupEditor);
                        }
                        if (rangeMarker.isValid()) {
                            rangeMarker.dispose();
                        }
                    }
                })
                .createPopup();

        tempDocument.addDocumentListener(new DocumentListener() {
            private boolean isUpdating = false;

            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (isUpdating || !rangeMarker.isValid()) return;
                isUpdating = true;
                try {
                    String newText = Arrays.stream(tempDocument.getText().split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining(" "));

                    WriteCommandAction.runWriteCommandAction(project, "Sync Tailwind Classes", null, () ->
                            editor.getDocument().replaceString(rangeMarker.getStartOffset(), rangeMarker.getEndOffset(), newText));
                    updatePopupSize(popup, popupEditor);
                } finally {
                    isUpdating = false;
                }
            }
        });

        popup.showInBestPositionFor(editor);
        updatePopupSize(popup, popupEditor);
    }

    private void updatePopupSize(JBPopup popup, EditorEx popupEditor) {
        if (popup.isDisposed()) return;
        int lineCount = popupEditor.getDocument().getLineCount();
        int lineHeight = popupEditor.getLineHeight();
        int height = Math.min(600, Math.max(200, lineCount * lineHeight + 50));
        popup.setSize(new Dimension(400, height));
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
