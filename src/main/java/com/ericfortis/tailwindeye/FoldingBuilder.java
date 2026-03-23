package com.ericfortis.tailwindeye;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FoldingBuilder extends FoldingBuilderEx {

	public static final FoldingGroup TAILWIND_GROUP = FoldingGroup.newGroup("TailwindEyeFoldingGroup");
	private static final Pattern CLASS_NAME_CONTENT_PATTERN = Pattern.compile("className\\s*[=:]\\s*([\"'])(.*?)\\1");

	@Override
	public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
		if (!(root instanceof PsiFile))
			return FoldingDescriptor.EMPTY_ARRAY;

		List<FoldingDescriptor> descriptors = new ArrayList<>();
		String text = root.getText();
		Matcher matcher = CLASS_NAME_CONTENT_PATTERN.matcher(text);

		while (matcher.find()) {
			int start = matcher.start(2);
			int end = matcher.end(2);
			if (start < end)
				descriptors.add(new FoldingDescriptor(root.getNode(), new TextRange(start, end), TAILWIND_GROUP));
		}

		return descriptors.toArray(new FoldingDescriptor[0]);
	}

	@Override
	public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
		return "...";
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode node) {
		Project project = node.getPsi().getProject();
		CoreState.FadingMode mode = CoreState.getInstance(project).getFadingMode();
		return mode == CoreState.FadingMode.FOLD_CLASS_NAME;
	}
}
