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

		XmlAttributeValue attr = findClassNameAttrValue(element);
		if (attr == null) return;

		String rawText = attr.getText();
		String replacement = toggle(rawText);
		if (replacement == null) return;

		TextRange range = attr.getTextRange();
		WriteCommandAction.runWriteCommandAction(project, "Toggle ClassName", null, () ->
			 editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), replacement));
	}

	private static XmlAttributeValue findClassNameAttrValue(PsiElement element) {
		for (PsiElement current = element; current != null && !(current instanceof PsiFile); current = current.getParent())
			if (current instanceof XmlAttributeValue value
				 && value.getParent() instanceof XmlAttribute attribute
				 && "className".equals(attribute.getName())
			)
				return value;
		return null;
	}

	static String toggle(String text) {
		if (text.startsWith("{`") && text.endsWith("`}"))
			return toInline(text);
		if (text.startsWith("\"") && text.endsWith("\""))
			return toMultiline(text);
		return null;
	}

	private static String toInline(String text) {
		String content = text.substring(2, text.length() - 2).trim();
		return content.isBlank()
			 ? null
			 : "\"" + normalizeSpaces(content) + "\"";
	}

	private static String toMultiline(String text) {
		String content = text.substring(1, text.length() - 1).trim();
		return content.isBlank()
			 ? null
			 : "{`\n" + splitOnWhitespace(content) + "\n`}";
	}

	private static String normalizeSpaces(String text) {
		return splitOnWhitespace(text).replace("\n", " ");
	}

	private static String splitOnWhitespace(String text) {
		return Arrays.stream(text.split("\\s+"))
			 .filter(s -> !s.isEmpty())
			 .collect(Collectors.joining("\n"));
	}
}