package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Refactors a className into a row or column. IOW, inline or multiline.
 */
public class ActionTranspose extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		var project = e.getProject();
		var editor = e.getData(CommonDataKeys.EDITOR);
		var psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if (project == null || editor == null || psiFile == null) return;

		var element = psiFile.findElementAt(editor.getCaretModel().getOffset());
		if (element == null) return;

		var attr = findClassNameAttrValue(element);
		if (attr == null) return;

		var replacement = toggle(attr.getText());
		if (replacement == null) return;

		var range = attr.getTextRange();
		WriteCommandAction.runWriteCommandAction(project, "Transpose ClassName", null, () ->
			 editor.getDocument().replaceString(range.getStartOffset(), range.getEndOffset(), replacement));
	}

	private static XmlAttributeValue findClassNameAttrValue(PsiElement element) {
		for (PsiElement current = element; current != null && !(current instanceof PsiFile); current = current.getParent())
			if (current instanceof XmlAttributeValue v
				 && v.getParent() instanceof XmlAttribute attr
				 && "className".equals(attr.getName()))
				return v;
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
		return "\"" + normalizeSpaces(text.substring(2, text.length() - 2).trim()) + "\"";
	}

	private static String toMultiline(String text) {
		return "{`\n" + splitOnWhitespace(text.substring(1, text.length() - 1).trim()) + "\n`}";
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