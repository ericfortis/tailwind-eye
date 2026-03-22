package com.github.ericfortis.tailwindeye;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class Annotator implements com.intellij.lang.annotation.Annotator {

    private static final TextAttributesKey FADED_TEXT = TextAttributesKey.createTextAttributesKey(
            "TAILWIND_EYE_TEXT"
    );

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiFile psiFile)) return;

        Project project = psiFile.getProject();
        CoreState.FadingMode mode = CoreState.getInstance(project).getFadingMode();
        
        String text = psiFile.getText();

        if (mode == CoreState.FadingMode.NON_STYLING) {
            String[] lines = text.split("\n");
            int lineOffset = 0;

            for (String line : lines) {
                if (!line.contains("className")) {
                    int lineLength = line.length();
                    if (lineLength > 0) {
                        holder.newAnnotation(HighlightSeverity.INFORMATION, "")
                                .range(new TextRange(lineOffset, lineOffset + lineLength))
                                .textAttributes(FADED_TEXT)
                                .create();
                    }
                }
                lineOffset += line.length() + 1; // +1 for the \n
            }
        }
    }
}
