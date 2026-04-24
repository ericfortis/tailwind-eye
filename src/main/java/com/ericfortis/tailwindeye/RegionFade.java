package com.ericfortis.tailwindeye;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.lang.Boolean.TRUE;


public final class RegionFade {

	private static final TextAttributesKey FADED_TEXT = TextAttributesKey.createTextAttributesKey(
		 "TAILWIND_EYE_FAINT"
	);
	private static final Key<Boolean> TAILWIND_EYE_FADE_MARKER = Key.create("TAILWIND_EYE_FADE_MARKER");

	private RegionFade() {
	}

	public static void updateFade(@NotNull Editor editor, @NotNull PsiFile psiFile, boolean enabled) {
		var markupModel = editor.getMarkupModel();
		clearTailwindFadeHighlighters(markupModel);

		if (!enabled) return;

		for (var range : getFadeRanges(psiFile)) {
			var highlighter = markupModel.addRangeHighlighter(
				 range.getStartOffset(),
				 range.getEndOffset(),
				 HighlighterLayer.WARNING,
				 null,
				 HighlighterTargetArea.EXACT_RANGE
			);
			highlighter.setTextAttributesKey(FADED_TEXT);
			highlighter.putUserData(TAILWIND_EYE_FADE_MARKER, true);
		}
	}

	private static void clearTailwindFadeHighlighters(MarkupModel markupModel) {
		for (var highlighter : markupModel.getAllHighlighters())
			if (TRUE.equals(highlighter.getUserData(TAILWIND_EYE_FADE_MARKER)))
				markupModel.removeHighlighter(highlighter);
	}

	private static List<TextRange> getFadeRanges(@NotNull PsiFile psiFile) {
		List<TextRange> keepRanges = new ArrayList<>();

		psiFile.accept(new PsiRecursiveElementWalkingVisitor() {
			@Override
			public void visitElement(@NotNull PsiElement element) {
				if (element instanceof XmlTag tag)
					visitTag(tag, keepRanges);
				super.visitElement(element);
			}
		});

		keepRanges.sort(Comparator.comparingInt(TextRange::getStartOffset));
		int len = psiFile.getTextLength();
		List<TextRange> fadeRanges = new ArrayList<>();
		if (keepRanges.isEmpty()) {
			if (len > 0)
				fadeRanges.add(new TextRange(0, len));
			return fadeRanges;
		}

		int lastEnd = 0;
		for (var range : keepRanges) {
			if (range.getStartOffset() > lastEnd)
				fadeRanges.add(new TextRange(lastEnd, range.getStartOffset()));
			lastEnd = Math.max(lastEnd, range.getEndOffset());
		}

		if (lastEnd < len)
			fadeRanges.add(new TextRange(lastEnd, len));

		return fadeRanges;
	}

	private static void visitTag(XmlTag tag, List<TextRange> keepRanges) {
		keepRanges.add(tag.getFirstChild().getNextSibling().getTextRange()); // tag name

		var attr = tag.getAttribute("className");
		if (attr != null) {
			var value = attr.getValueElement();
			if (value != null)
				keepRanges.add(value.getValueTextRange());
		}
	}
}
