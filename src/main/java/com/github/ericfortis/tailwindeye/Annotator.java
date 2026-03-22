package com.github.ericfortis.tailwindeye;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Annotator implements com.intellij.lang.annotation.Annotator {

    private static final TextAttributesKey FADED_TEXT = TextAttributesKey.createTextAttributesKey(
            "TAILWIND_EYE_TEXT"
    );

    private static final Pattern OPENING_TAG_PATTERN = Pattern.compile("<[a-zA-Z0-9_-]+");
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("className\\s*[=:]\\s*([\"'])(.*?)\\1");

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiFile psiFile))
            return;

        Project project = psiFile.getProject();
        CoreState.FadingMode mode = CoreState.getInstance(project).getFadingMode();

        if (mode != CoreState.FadingMode.NON_STYLING)
            return;

        String text = psiFile.getText();
        List<TextRange> keepRanges = new ArrayList<>();

        Matcher tagMatcher = OPENING_TAG_PATTERN.matcher(text);
        while (tagMatcher.find())
            keepRanges.add(new TextRange(tagMatcher.start(), tagMatcher.end()));

        Matcher classMatcher = CLASS_NAME_PATTERN.matcher(text);
        while (classMatcher.find())
            if (classMatcher.groupCount() >= 2)
                keepRanges.add(new TextRange(classMatcher.start(2), classMatcher.end(2)));

        if (keepRanges.isEmpty()) {
            if (!text.isEmpty())
                annotateFaded(0, text.length(), holder);
            return;
        }

        // Sort keepRanges by start offset
        keepRanges.sort(Comparator.comparingInt(TextRange::getStartOffset));

        int lastEnd = 0;
        for (TextRange range : keepRanges) {
            if (range.getStartOffset() > lastEnd)
                annotateFaded(lastEnd, range.getStartOffset(), holder);
            lastEnd = Math.max(lastEnd, range.getEndOffset());
        }

        if (lastEnd < text.length())
            annotateFaded(lastEnd, text.length(), holder);
    }

    private void annotateFaded(int start, int end, AnnotationHolder holder) {
        if (start < end)
            holder.newAnnotation(HighlightSeverity.INFORMATION, "")
                    .range(new TextRange(start, end))
                    .textAttributes(FADED_TEXT)
                    .create();
    }
}
