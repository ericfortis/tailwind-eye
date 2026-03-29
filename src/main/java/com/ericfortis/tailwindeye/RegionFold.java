package com.ericfortis.tailwindeye;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SyntaxTraverser;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegionFold extends FoldingBuilderEx {

	public static final FoldingGroup TAILWIND_GROUP = FoldingGroup.newGroup("TailwindEyeFoldingGroup");

	@Override
	public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
		return SyntaxTraverser.psiTraverser(root)
			 .filter(XmlAttribute.class)
			 .filter(attr -> "className".equals(attr.getName())
					&& attr.getValueElement() != null
					&& attr.getValue() != null
					&& !attr.getValue().isBlank())
			 .map(attr -> {
				 var fullRange = attr.getValueElement().getTextRange();
				 var innerRange = new TextRange(fullRange.getStartOffset() + 1, fullRange.getEndOffset() - 1);
				 return new FoldingDescriptor(attr.getNode(), innerRange, TAILWIND_GROUP);
			 })
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
