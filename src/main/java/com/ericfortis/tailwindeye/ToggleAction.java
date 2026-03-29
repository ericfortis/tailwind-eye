package com.ericfortis.tailwindeye;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import org.jetbrains.annotations.NotNull;

public class ToggleAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		var editor = e.getData(CommonDataKeys.EDITOR);
		var project = e.getProject();
		var psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if (editor == null || project == null || psiFile == null) return;

		var vFile = psiFile.getVirtualFile();
		if (vFile == null) return;

		if ("js".equals(vFile.getExtension())) return; // because we can't register the plugin exclusively for JSX

		var coreState = CoreState.getInstance(project);
		CoreState.FadingMode currentMode = vFile.getUserData(CoreState.FADING_MODE_KEY);
		if (currentMode == null)
			currentMode = coreState.getMode();

		CoreState.FadingMode nextMode = currentMode.next();
		vFile.putUserData(CoreState.FADING_MODE_KEY, nextMode);

		boolean shouldExpand = nextMode != CoreState.FadingMode.FOLD_CLASS_NAME;

		DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "tailwind eye toggle");

		var fm = (FoldingModelEx) editor.getFoldingModel();
		fm.runBatchFoldingOperation(() -> {
			for (FoldRegion r : fm.getGroupedRegions(ClassNameFolding.TAILWIND_GROUP))
				r.setExpanded(shouldExpand);
		});
	}
}
