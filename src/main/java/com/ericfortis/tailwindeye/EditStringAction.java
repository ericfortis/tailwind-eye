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
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.LocalTimeCounter;
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
        String classesContent = Arrays.stream(originalContent.split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));


        // Create a dummy HTML file as it is a common host for Tailwind CSS
        String popupContent = "<div class=\"" + classesContent + "\"></div>";
        FileType htmlFileType = FileTypeManager.getInstance().getFileTypeByExtension("html");
        PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.html", htmlFileType, popupContent, LocalTimeCounter.currentTime(), true);

        // Crucial: help Tailwind find the context it needs (like node_modules, tailwind.config.js)
        dummyFile.putUserData(com.intellij.openapi.util.Key.create("original.psi.file"), psiFile);

        final Document tempDocument = PsiDocumentManager.getInstance(project).getDocument(dummyFile) != null
                ? PsiDocumentManager.getInstance(project).getDocument(dummyFile)
                : EditorFactory.getInstance().createDocument(popupContent);

        EditorFactory editorFactory = EditorFactory.getInstance();
        EditorEx popupEditor = (EditorEx) editorFactory.createEditor(tempDocument, project, htmlFileType, false);
        
        popupEditor.getSettings().setLineNumbersShown(true);
        popupEditor.getSettings().setFoldingOutlineShown(false);
        popupEditor.getComponent().setPreferredSize(new Dimension(400, 300));

        JBPopup popup = JBPopupFactory.getInstance()
                .createComponentPopupBuilder(popupEditor.getComponent(), popupEditor.getContentComponent())
                .setFocusable(true)
                .setRequestFocus(true)
                .setResizable(true)
                .setMovable(true)
                .setTitle("Edit Tailwind Classes")
                .setCancelCallback(() -> {
                    editorFactory.releaseEditor(popupEditor);
                    rangeMarker.dispose();
                    return true;
                })
                .createPopup();

        tempDocument.addDocumentListener(new DocumentListener() {
            private boolean isUpdating = false;

            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (isUpdating || !rangeMarker.isValid()) return;
                isUpdating = true;
                try {
                    String fullText = tempDocument.getText();
                    String contentToSync = extractClasses(fullText);
                    
                    String newText = Arrays.stream(contentToSync.split("\n"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.joining(" "));
                    
                    WriteCommandAction.runWriteCommandAction(project, "Sync Tailwind Classes", null, () -> {
                        editor.getDocument().replaceString(rangeMarker.getStartOffset(), rangeMarker.getEndOffset(), newText);
                    });
                    updatePopupSize(popup, popupEditor);
                } finally {
                    isUpdating = false;
                }
            }
        });

        popup.showInBestPositionFor(editor);
        updatePopupSize(popup, popupEditor);
    }

    private String extractClasses(String fullText) {
        int start = fullText.indexOf("class=\"");
        if (start == -1) return fullText;
        start += "class=\"".length();
        int end = fullText.indexOf("\"", start);
        if (end == -1) return fullText.substring(start);
        return fullText.substring(start, end);
    }

    private void updatePopupSize(JBPopup popup, EditorEx popupEditor) {
        if (popup.isDisposed()) return;
        Dimension preferredSize = popupEditor.getComponent().getPreferredSize();
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
