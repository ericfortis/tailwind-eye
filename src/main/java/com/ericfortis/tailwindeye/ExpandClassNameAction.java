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

		XmlAttributeValue cnameVal = findClosestClassNameAttr(element);
		if (cnameVal == null) return;

		// Get the range of the attribute value including quotes
		TextRange rangeToUse = cnameVal.getTextRange();
		String rawTextToUse = cnameVal.getText();

		// Handle expansion (from double quotes to multiline)
		if (rawTextToUse.startsWith("\"") && rawTextToUse.endsWith("\"")) {
			String content = rawTextToUse.substring(1, rawTextToUse.length() - 1);
			if (content.isBlank()) return;

			String[] classes = content.split("\\s+");
			String newContent = Arrays.stream(classes)
				 .filter(s -> !s.isEmpty())
				 .collect(Collectors.joining("\n"));

			String replacement = "{`\n" + newContent + "\n`}";

			WriteCommandAction.runWriteCommandAction(project, "Expand ClassName", null, () ->
				 editor.getDocument().replaceString(rangeToUse.getStartOffset(), rangeToUse.getEndOffset(), replacement));
			return;
		}

		// Handle inlining (from multiline to double quotes)
		String trimmedRaw = rawTextToUse.trim();
		if (trimmedRaw.startsWith("{") && trimmedRaw.endsWith("}")) {
			String content = trimmedRaw.substring(1, trimmedRaw.length() - 1).trim();
			if (content.startsWith("`") && content.endsWith("`")) {
				content = content.substring(1, content.length() - 1).trim();

				if (content.isBlank()) return;

				String[] classes = content.split("\\s+");
				String newContent = Arrays.stream(classes)
					 .filter(s -> !s.isEmpty())
					 .collect(Collectors.joining(" "));

				String replacement = "\"" + newContent + "\"";

				WriteCommandAction.runWriteCommandAction(project, "Inline ClassName", null, () ->
					 editor.getDocument().replaceString(rangeToUse.getStartOffset(), rangeToUse.getEndOffset(), replacement));
			}
		}
	}

	private XmlAttributeValue findClosestClassNameAttr(PsiElement element) {
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
}
