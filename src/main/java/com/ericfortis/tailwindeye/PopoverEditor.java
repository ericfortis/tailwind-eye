package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PopoverEditor extends AnAction {

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

		int relativeCaretOffset = offset - foundRange.getStartOffset();
		PopupContentResult result = computePopupContent(originalContent, relativeCaretOffset);
		EditorTextField editorTextField = getEditorTextField(result.content(), project, result.caretOffset());

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
		}, popup);

		popup.showInBestPositionFor(editor);
		updatePopupSize(popup, editorTextField);
	}

	private record PopupContentResult(String content, int caretOffset) {}

	private PopupContentResult computePopupContent(String originalContent, int relativeCaretOffset) {
		StringBuilder popupContentBuilder = new StringBuilder();
		int popupCaretOffset = -1;

		Matcher m = Pattern.compile("\\S+").matcher(originalContent);
		int currentPopupLength = 0;

		while (m.find()) {
			String token = m.group();
			int tokenStart = m.start();
			int tokenEnd = m.end();

			if (!popupContentBuilder.isEmpty()) {
				popupContentBuilder.append("\n");
				currentPopupLength++;
			}

			int tokenStartInPopup = currentPopupLength;
			popupContentBuilder.append(token);
			currentPopupLength += token.length();

			if (popupCaretOffset == -1) {
				if (relativeCaretOffset < tokenStart) {
					popupCaretOffset = tokenStartInPopup;
				} else if (relativeCaretOffset <= tokenEnd) {
					int diff = relativeCaretOffset - tokenStart;
					popupCaretOffset = tokenStartInPopup + diff;
				}
			}
		}

		if (popupCaretOffset == -1) {
			popupCaretOffset = popupContentBuilder.length();
		}

		return new PopupContentResult(popupContentBuilder.toString(), popupCaretOffset);
	}

	private static @NotNull EditorTextField getEditorTextField(String popupContent, Project project, int popupCaretOffset) {
		EditorTextField editorTextField = new EditorTextField(popupContent, project, FileTypeManager.getInstance().getStdFileType("HTML"));
		editorTextField.setOneLineMode(false);
		editorTextField.setPreferredSize(new Dimension(400, 300));

		editorTextField.addSettingsProvider(editorEx -> {
			editorEx.getSettings().setFoldingOutlineShown(false);
			editorEx.getSettings().setLineNumbersShown(false);
			editorEx.getCaretModel().moveToOffset(Math.min(popupCaretOffset, editorEx.getDocument().getTextLength()));
		});
		return editorTextField;
	}

	private void updatePopupSize(JBPopup popup, EditorTextField editorTextField) {
		if (popup.isDisposed())
			return;
		Editor editor = editorTextField.getEditor();
		if (editor == null) return;

		int lineCount = editor.getDocument().getLineCount();
		int lineHeight = editor.getLineHeight();
		long height = Math.clamp((long) lineCount * lineHeight + 50, 200, 600);
		popup.setSize(new Dimension(400, (int) height));
	}

	private XmlAttributeValue findAttributeValue(PsiElement element) {
		PsiElement current = element;
		while (current != null && !(current instanceof PsiFile)) {
			if (current instanceof XmlAttributeValue value) {
				PsiElement parent = value.getParent();
				if (parent instanceof XmlAttribute attribute) {
					String name = attribute.getName();
					if ("className".equals(name))
						return value;
				}
			}
			current = current.getParent();
		}
		return null;
	}
}
