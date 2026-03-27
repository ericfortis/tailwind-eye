package com.ericfortis.tailwindeye;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SyntaxFader implements com.intellij.lang.annotation.Annotator {

	private static final TextAttributesKey FADED_TEXT = TextAttributesKey.createTextAttributesKey(
		 "TAILWIND_EYE_TEXT"
	);

	@Override
	public void annotate(@NotNull PsiElement root, @NotNull AnnotationHolder holder) {
		if (!(root instanceof PsiFile psiFile))
			return;

		if (!CoreState.getInstance(psiFile.getProject()).isFading())
			return;

		List<TextRange> keepRanges = new ArrayList<>();

		root.accept(new PsiRecursiveElementWalkingVisitor() {
			@Override
			public void visitElement(@NotNull PsiElement element) {
				if (element instanceof XmlTag tag)
					visitTag(tag, keepRanges);
				super.visitElement(element);
			}
		});

		String text = psiFile.getText();
		if (keepRanges.isEmpty()) {
			if (!text.isEmpty())
				annotateFaded(0, text.length(), holder);
			return;
		}

		int lastEnd = 0;
		for (TextRange range : keepRanges) {
			if (range.getStartOffset() > lastEnd)
				annotateFaded(lastEnd, range.getStartOffset(), holder);
			lastEnd = Math.max(lastEnd, range.getEndOffset());
		}

		if (lastEnd < text.length())
			annotateFaded(lastEnd, text.length(), holder);
	}

	private void visitTag(XmlTag tag, List<TextRange> keepRanges) {
		keepRanges.add(tag.getFirstChild().getNextSibling().getTextRange()); // tag name

		XmlAttribute attribute = tag.getAttribute("className");
		if (attribute != null) {
			XmlAttributeValue value = attribute.getValueElement();
			if (value != null)
				keepRanges.add(value.getValueTextRange());
		}
	}

	private void annotateFaded(int start, int end, AnnotationHolder holder) {
		if (start < end)
			holder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
				 .range(new TextRange(start, end))
				 .textAttributes(FADED_TEXT)
				 .create();
	}
}
