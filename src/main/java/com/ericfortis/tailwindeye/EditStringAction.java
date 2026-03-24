package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.ui.EditorTextField;
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

		// Use PSI to find the closest attribute value
		XmlAttributeValue attributeValue = findAttributeValue(element);
		if (attributeValue == null) return;

		TextRange foundRange = attributeValue.getValueTextRange();
		if (foundRange.isEmpty()) return;

		// Use a RangeMarker to track the range as the document is edited
		com.intellij.openapi.editor.RangeMarker rangeMarker = editor.getDocument().createRangeMarker(foundRange);

		String originalContent = editor.getDocument().getText(foundRange);
		String popupContent = Arrays.stream(originalContent.split("\\s+"))
			 .filter(s -> !s.isEmpty())
			 .collect(Collectors.joining("\n"));

		EditorTextField editorTextField = new EditorTextField(popupContent, project, PlainTextFileType.INSTANCE);
		editorTextField.setOneLineMode(false);
		editorTextField.setPreferredSize(new Dimension(400, 300));
		
		editorTextField.addSettingsProvider(editorEx -> {
			editorEx.getSettings().setFoldingOutlineShown(false);
			editorEx.getSettings().setLineNumbersShown(false);
		});

		JBPopup popup = JBPopupFactory.getInstance()
			 .createComponentPopupBuilder(editorTextField, editorTextField)
			 .setFocusable(true)
			 .setRequestFocus(true)
			 .setResizable(true)
			 .setMovable(true)
			 .createPopup();

		Document tempDocument = editorTextField.getDocument();
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
					
					updatePopupSize(popup, editorTextField);
				} finally {
					isUpdating = false;
				}
			}
		});

		popup.showInBestPositionFor(editor);
		updatePopupSize(popup, editorTextField);
	}

	private void updatePopupSize(JBPopup popup, EditorTextField editorTextField) {
		if (popup.isDisposed())
			return;
		Editor editor = editorTextField.getEditor();
		if (editor == null) return;
		
		int lineCount = editor.getDocument().getLineCount();
		int lineHeight = editor.getLineHeight();
		int height = Math.min(600, Math.max(200, lineCount * lineHeight + 50));
		popup.setSize(new Dimension(400, height));
	}

	private XmlAttributeValue findAttributeValue(PsiElement element) {
		PsiElement current = element;
		while (current != null && !(current instanceof PsiFile)) {
			if (current instanceof XmlAttributeValue value) {
				PsiElement parent = value.getParent();
				if (parent instanceof XmlAttribute attribute) {
					String name = attribute.getName();
					if ("className".equals(name) || "class".equals(name)) {
						return value;
					}
				}
			}
			current = current.getParent();
		}
		return null;
	}
}
