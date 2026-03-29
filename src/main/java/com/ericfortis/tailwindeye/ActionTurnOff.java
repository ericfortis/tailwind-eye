package com.ericfortis.tailwindeye;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import org.jetbrains.annotations.NotNull;

public class ActionTurnOff extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		var project = e.getProject();
		var editor = e.getData(CommonDataKeys.EDITOR);
		var psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if (project == null || editor == null || psiFile == null) return;

		var vFile = psiFile.getVirtualFile();
		if (vFile == null) return;

		vFile.putUserData(CoreState.FADING_MODE_KEY, CoreState.FadingMode.OFF);

		DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "tailwind eye off");

		var fm = (FoldingModelEx) editor.getFoldingModel();
		fm.runBatchFoldingOperation(() -> {
			for (FoldRegion r : fm.getGroupedRegions(RegionFold.TAILWIND_GROUP))
				r.setExpanded(true);
		});
	}
}
