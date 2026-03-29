package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExpandClassNameAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if (project == null || editor == null || psiFile == null) return;

		int offset = editor.getCaretModel().getOffset();
		PsiElement element = psiFile.findElementAt(offset);
		if (element == null) return;

		XmlAttributeValue attr = findClosestClassNameAttrValue(element);
		if (attr == null) return;

		TextRange range = attr.getTextRange();
		String rawText = attr.getText();

		if (caretInMultiline(rawText)) {
			String replacement = toInline(rawText);
			if (replacement == null) return;
			WriteCommandAction.runWriteCommandAction(project, "Inline ClassName", null, () ->
				 editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), replacement));
		} else if (caretInInline(rawText)) {
			String replacement = toMultiline(rawText);
			if (replacement == null) return;
			WriteCommandAction.runWriteCommandAction(project, "Expand ClassName", null, () ->
				 editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), replacement));
		}
	}

	private XmlAttributeValue findClosestClassNameAttrValue(PsiElement element) {
		PsiElement current = element;
		while (current != null && !(current instanceof PsiFile)) {
			if (current instanceof XmlAttributeValue value) {
				PsiElement parent = value.getParent();
				if (parent instanceof XmlAttribute attribute && "className".equals(attribute.getName()))
					return value;
			}
			current = current.getParent();
		}
		return null;
	}

	private boolean caretInMultiline(String text) {
		return text.startsWith("{`") && text.endsWith("`}");
	}

	private boolean caretInInline(String text) {
		return text.startsWith("\"") && text.endsWith("\"");
	}

	private String toInline(String text) {
		String t = text.trim();
		String content = t.substring(1, t.length() - 1).trim();
		content = content.substring(1, content.length() - 1).trim();

		if (content.isBlank()) return null;

		String newContent = Arrays.stream(content.split("\\s+"))
			 .filter(s -> !s.isEmpty())
			 .collect(Collectors.joining(" "));

		return "\"" + newContent + "\"";
	}

	private String toMultiline(String text) {
		String content = text.substring(1, text.length() - 1);
		if (content.isBlank()) return null;

		String newContent = Arrays.stream(content.split("\\s+"))
			 .filter(s -> !s.isEmpty())
			 .collect(Collectors.joining("\n"));

		return "{`\n" + newContent + "\n`}";
	}
}
