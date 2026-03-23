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
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FoldingBuilder extends FoldingBuilderEx {

	public static final FoldingGroup TAILWIND_GROUP = FoldingGroup.newGroup("TailwindEyeFoldingGroup");

	@Override
	public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
		if (!(root instanceof PsiFile))
			return FoldingDescriptor.EMPTY_ARRAY;

		List<FoldingDescriptor> descriptors = new ArrayList<>();

		root.accept(new PsiRecursiveElementWalkingVisitor() {
			@Override
			public void visitElement(@NotNull PsiElement element) {
				if (element instanceof XmlAttribute attribute) {
					String name = attribute.getName();
					if ("className".equals(name) || "class".equals(name)) {
						XmlAttributeValue value = attribute.getValueElement();
						if (value != null) {
							TextRange range = value.getValueTextRange();
							if (range.getStartOffset() < range.getEndOffset()) {
								descriptors.add(new FoldingDescriptor(element.getNode(), range, TAILWIND_GROUP));
							}
						}
					}
				}
				super.visitElement(element);
			}
		});

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
