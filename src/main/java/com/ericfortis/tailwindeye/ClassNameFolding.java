package com.ericfortis.tailwindeye;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClassNameFolding extends FoldingBuilderEx {

	public static final FoldingGroup TAILWIND_GROUP = FoldingGroup.newGroup("TailwindEyeFoldingGroup");

	@Override
	public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
		if (!(root instanceof PsiFile))
			return FoldingDescriptor.EMPTY_ARRAY;

		return SyntaxTraverser.psiTraverser(root)
			 .filter(XmlAttribute.class)
			 .filter(attr -> "className".equals(attr.getName()))
			 .map(attr -> new FoldingDescriptor(attr.getNode(), attr.getTextRange(), TAILWIND_GROUP))
			 .toList()
			 .toArray(new FoldingDescriptor[0]);
	}

	@Override
	public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
		return "…";
	}

	@Override
	public boolean isCollapsedByDefault(@NotNull ASTNode node) {
		return true;
	}
}
