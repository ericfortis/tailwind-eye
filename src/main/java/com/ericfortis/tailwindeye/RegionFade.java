package com.ericfortis.tailwindeye;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RegionFade implements Annotator, DumbAware {

	private static final TextAttributesKey FADED_TEXT = TextAttributesKey.createTextAttributesKey(
		 "TAILWIND_EYE_FAINT"
	);
	private static final TextAttributesKey CLASSNAMES_TEXT = TextAttributesKey.createTextAttributesKey(
		 "TAILWIND_EYE_CLASSNAMES"
	);

	@Override
	public void annotate(@NotNull PsiElement root, @NotNull AnnotationHolder holder) {
		if (!(root instanceof PsiFile psiFile))
			return;

		var vFile = psiFile.getVirtualFile();
		CoreState.FadingMode mode = null;
		if (vFile != null)
			mode = vFile.getUserData(CoreState.FADING_MODE_KEY);

		if (mode == null)
			mode = CoreState.getInstance(psiFile.getProject()).getMode();

		if (mode != CoreState.FadingMode.NON_STYLING)
			return;

		List<TextRange> keepRanges = new ArrayList<>();

		root.accept(new PsiRecursiveElementWalkingVisitor() {
			@Override
			public void visitElement(@NotNull PsiElement element) {
				if (element instanceof XmlTag tag)
					visitTag(tag, keepRanges, holder);
				super.visitElement(element);
			}
		});

		int textLength = psiFile.getTextLength();
		if (keepRanges.isEmpty()) {
			if (textLength > 0)
				annotateFaint(0, textLength, holder);
			return;
		}

		int lastEnd = 0;
		for (TextRange range : keepRanges) {
			if (range.getStartOffset() > lastEnd)
				annotateFaint(lastEnd, range.getStartOffset(), holder);
			lastEnd = Math.max(lastEnd, range.getEndOffset());
		}

		if (lastEnd < textLength)
			annotateFaint(lastEnd, textLength, holder);
	}

	private void visitTag(XmlTag tag, List<TextRange> keepRanges, AnnotationHolder holder) {
		keepRanges.add(tag.getFirstChild().getNextSibling().getTextRange()); // tag name

		var attribute = tag.getAttribute("className");
		if (attribute != null) {
			var value = attribute.getValueElement();
			if (value != null) {
				keepRanges.add(value.getValueTextRange());
				annotateClassName(keepRanges.getLast(), holder);
			}
		}
	}

	private void annotateClassName(TextRange range, AnnotationHolder holder) {
		holder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
			 .range(range)
			 .textAttributes(CLASSNAMES_TEXT)
			 .create();
	}

	private void annotateFaint(int start, int end, AnnotationHolder holder) {
		if (start < end)
			holder.newAnnotation(HighlightSeverity.TEXT_ATTRIBUTES, "")
				 .range(new TextRange(start, end))
				 .textAttributes(FADED_TEXT)
				 .create();
	}
}
