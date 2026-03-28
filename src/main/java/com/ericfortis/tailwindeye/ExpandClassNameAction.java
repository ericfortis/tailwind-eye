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

		// Use PSI to find the closest attribute value
		PsiElement attributeValue = findAttributeValue(element);
		if (attributeValue == null) return;

		// Get the range of the attribute value including quotes
		TextRange range = attributeValue.getTextRange();
		String originalContent;
		if (attributeValue instanceof XmlAttributeValue xmlValue) {
			originalContent = xmlValue.getValue();
		} else {
			// Fallback for non-XML/HTML elements that we identified as className
			originalContent = attributeValue.getText();
			// Remove curly braces if it's JSX { "..." }
			if (originalContent.startsWith("{") && originalContent.endsWith("}")) {
				originalContent = originalContent.substring(1, originalContent.length() - 1).trim();
			}
			// Remove quotes (", ', or `)
			if ((originalContent.startsWith("\"") && originalContent.endsWith("\"")) ||
				 (originalContent.startsWith("'") && originalContent.endsWith("'")) ||
				 (originalContent.startsWith("`") && originalContent.endsWith("`"))) {
				originalContent = originalContent.substring(1, originalContent.length() - 1);
			}
		}

		if (originalContent == null || originalContent.isBlank()) return;

		String[] classes = originalContent.split("\\s+");
		String newContent = Arrays.stream(classes)
			 .filter(s -> !s.isEmpty())
			 .collect(Collectors.joining("\n"));

		String replacement = "{`\n" + newContent + "\n`}";

		// Adjust range if we are already in curly braces or quotes that we removed from originalContent
		TextRange finalRange = range;

		WriteCommandAction.runWriteCommandAction(project, "Expand ClassName", null, () ->
			 editor.getDocument().replaceString(finalRange.getStartOffset(), finalRange.getEndOffset(), replacement));
	}

	private PsiElement findAttributeValue(PsiElement element) {
		PsiElement current = element;
		while (current != null && !(current instanceof PsiFile)) {
			// XML/HTML support
			if (current instanceof XmlAttributeValue value) {
				PsiElement parent = value.getParent();
				if (parent instanceof XmlAttribute attribute) {
					if ("className".equals(attribute.getName()))
						return value;
				}
			}

			// JSX/JS support (also handles cases where the parser might not be fully configured in tests)
			String text = current.getText();
			PsiElement parent = current.getParent();
			if (parent != null) {
				String parentText = parent.getText();
				// Basic check for className="..." or className={...}
				if (parentText.startsWith("className=") && 
					 (text.startsWith("\"") || text.startsWith("'") || text.startsWith("{") || text.startsWith("`"))) {
					return current;
				}
				
				// Handle if we are inside the string of a className={ "..." }
				PsiElement grandParent = parent.getParent();
				if (grandParent != null) {
					String grandParentText = grandParent.getText();
					if (grandParentText.startsWith("className=") && parentText.startsWith("{")) {
						return parent;
					}
				}
			}

			current = current.getParent();
		}
		return null;
	}
}
