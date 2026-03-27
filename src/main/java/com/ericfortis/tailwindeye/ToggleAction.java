package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ToggleAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (editor == null) return;

		Project project = e.getProject();
		if (project == null) return;

		CoreState.FadingMode nextMode = CoreState.getInstance(project).toggle();
		boolean shouldExpand = nextMode != CoreState.FadingMode.FOLD_CLASS_NAME;

		FoldingModelEx foldingModel = (FoldingModelEx) editor.getFoldingModel();
		foldingModel.runBatchFoldingOperation(() -> {
			for (FoldRegion region : foldingModel.getGroupedRegions(ClassNameFolding.TAILWIND_GROUP))
				region.setExpanded(shouldExpand);
		});
	}
}
