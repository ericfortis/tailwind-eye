package com.ericfortis.tailwindeye;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class TurnOffAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		var editor = e.getData(CommonDataKeys.EDITOR);
		var project = e.getProject();
		var psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if (editor == null || project == null || psiFile == null) return;

		var vFile = psiFile.getVirtualFile();
		if (vFile == null) return;

		vFile.putUserData(CoreState.FADING_MODE_KEY, CoreState.FadingMode.OFF);

		DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "tailwind eye off");

		var fm = (FoldingModelEx) editor.getFoldingModel();
		fm.runBatchFoldingOperation(() -> {
			for (FoldRegion r : fm.getGroupedRegions(ClassNameFolding.TAILWIND_GROUP))
				r.setExpanded(true);
		});
	}
}
