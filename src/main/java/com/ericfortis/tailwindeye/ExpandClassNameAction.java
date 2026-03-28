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
		String rawText = attributeValue.getText();

		// If attributeValue is the XmlAttribute itself, we need to narrow down to the value part
		TextRange rangeToUse;
		String rawTextToUse;
		if (attributeValue instanceof XmlAttribute attribute) {
			int eqIdx = rawText.indexOf("=");
			if (eqIdx != -1) {
				int valueStart = eqIdx + 1;
				// Skip whitespace
				while (valueStart < rawText.length() && Character.isWhitespace(rawText.charAt(valueStart))) {
					valueStart++;
				}
				rangeToUse = new TextRange(range.getStartOffset() + valueStart, range.getEndOffset());
				rawTextToUse = rawText.substring(valueStart);
			} else {
				rangeToUse = range;
				rawTextToUse = rawText;
			}
		} else {
			rangeToUse = range;
			rawTextToUse = rawText;
		}

		// Handle expansion (from double quotes to multiline)
		if (rawTextToUse.startsWith("\"") && rawTextToUse.endsWith("\"")) {
			String content = rawTextToUse.substring(1, rawTextToUse.length() - 1);
			if (content.isBlank()) return;

			String[] classes = content.split("\\s+");
			String newContent = Arrays.stream(classes)
				 .filter(s -> !s.isEmpty())
				 .collect(Collectors.joining("\n"));

			String replacement = "{`\n" + newContent + "\n`}";

			TextRange finalRange = rangeToUse;
			WriteCommandAction.runWriteCommandAction(project, "Expand ClassName", null, () ->
				 editor.getDocument().replaceString(finalRange.getStartOffset(), finalRange.getEndOffset(), replacement));
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

				TextRange finalRange = rangeToUse;
				WriteCommandAction.runWriteCommandAction(project, "Inline ClassName", null, () ->
					 editor.getDocument().replaceString(finalRange.getStartOffset(), finalRange.getEndOffset(), replacement));
				return;
			}
		}

		// Check if it's just the backticked part
		TextRange workingRange = rangeToUse;
		if (trimmedRaw.startsWith("`")) {
			// Find actual end of backtick if it's truncated in this element
			if (!trimmedRaw.endsWith("`")) {
				String fullText = editor.getDocument().getText();
				int backtickEnd = fullText.indexOf("`", workingRange.getStartOffset() + 1);
				if (backtickEnd != -1) {
					workingRange = new TextRange(workingRange.getStartOffset(), backtickEnd + 1);
					trimmedRaw = fullText.substring(workingRange.getStartOffset(), workingRange.getEndOffset()).trim();
				}
			}
		}

		if (trimmedRaw.startsWith("`") && trimmedRaw.endsWith("`")) {
			// Try to find if there are curly braces { } surrounding this backticked string
			String fullText = editor.getDocument().getText();
			int start = workingRange.getStartOffset();
			int end = workingRange.getEndOffset();
			
			int braceStart = start;
			while (braceStart > 0 && (Character.isWhitespace(fullText.charAt(braceStart - 1)) || fullText.charAt(braceStart - 1) == '\n' || fullText.charAt(braceStart - 1) == '\r')) braceStart--;
			
			int braceEnd = end;
			while (braceEnd < fullText.length() && (Character.isWhitespace(fullText.charAt(braceEnd)) || fullText.charAt(braceEnd) == '\n' || fullText.charAt(braceEnd) == '\r')) braceEnd++;
			
			if (braceStart > 0 && fullText.charAt(braceStart - 1) == '{' && braceEnd < fullText.length() && fullText.charAt(braceEnd) == '}') {
				braceStart--;
				braceEnd++;
				
				TextRange braceRange = new TextRange(braceStart, braceEnd);
				String content = trimmedRaw.substring(1, trimmedRaw.length() - 1).trim();
				if (content.isBlank()) return;

				String[] classes = content.split("\\s+");
				String newContent = Arrays.stream(classes)
					 .filter(s -> !s.isEmpty())
					 .collect(Collectors.joining(" "));

				String replacement = "\"" + newContent + "\"";

				WriteCommandAction.runWriteCommandAction(project, "Inline ClassName", null, () ->
					 editor.getDocument().replaceString(braceRange.getStartOffset(), braceRange.getEndOffset(), replacement));
				return;
			}
		}
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
					 (text.startsWith("\"") || text.startsWith("{") || text.startsWith("`"))) {
					return current;
				}

				// If we are in HTML/XML but it's a JSX-like attribute value not parsed correctly
				// The XmlAttribute might contain everything after className= as a single token or multiple tokens
				if (parent instanceof XmlAttribute attribute && "className".equals(attribute.getName())) {
					return attribute;
				}
			}

			current = current.getParent();
		}
		return null;
	}
}
