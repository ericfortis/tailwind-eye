package com.ericfortis.tailwindeye;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ToggleAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) return;

		CoreState state = CoreState.getInstance(project);
		CoreState.FadingMode nextMode = state.getFadingMode() == CoreState.FadingMode.NON_STYLING
			 ? CoreState.FadingMode.FOLD_CLASS_NAME
			 : CoreState.FadingMode.NON_STYLING;

		state.setFadingMode(nextMode);
		boolean shouldExpand = nextMode != CoreState.FadingMode.FOLD_CLASS_NAME;

		DaemonCodeAnalyzer.getInstance(project).restart();

		Editor editor = e.getData(CommonDataKeys.EDITOR);
		if (editor != null) {
			FoldingModelEx foldingModel = (FoldingModelEx) editor.getFoldingModel();
			foldingModel.runBatchFoldingOperation(() -> {
				for (com.intellij.openapi.editor.FoldRegion region : foldingModel.getGroupedRegions(ClassNameFolding.TAILWIND_GROUP))
					region.setExpanded(shouldExpand);
			});
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) {
			e.getPresentation().setEnabled(false);
			return;
		}

		CoreState.FadingMode mode = CoreState.getInstance(project).getFadingMode();
		String text = switch (mode) {
			case NON_STYLING -> "Switch to Folding (ClassName)";
			case FOLD_CLASS_NAME -> "Switch to Fading (Non-Styling)";
		};
		e.getPresentation().setText(text);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}
}
