package com.ericfortis.tailwindeye;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class ActionFoldOrFade extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		var project = e.getProject();
		var editor = e.getData(CommonDataKeys.EDITOR);
		var psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if (project == null || editor == null || psiFile == null) return;

		var vFile = psiFile.getVirtualFile();
		if (vFile == null) return;

		if ("js".equals(vFile.getExtension())) return; // because we can't register the plugin exclusively for JSX

		var coreState = CoreState.getInstance(project);
		CoreState.FadingMode currentMode = vFile.getUserData(CoreState.FADING_MODE_KEY);
		if (currentMode == null)
			currentMode = coreState.getMode();

		CoreState.FadingMode nextMode = currentMode.next();

		setFade(vFile, nextMode);
		setFold(editor, nextMode != CoreState.FadingMode.FOLD_CLASS_NAME);
		DaemonCodeAnalyzer.getInstance(project).restart(psiFile, "tailwind eye toggle");
	}

	private static void setFade(VirtualFile vFile, CoreState.FadingMode nextMode) {
		vFile.putUserData(CoreState.FADING_MODE_KEY, nextMode);
	}

	public static void setFold(Editor editor, boolean shouldExpand) {
		var fm = (FoldingModelEx) editor.getFoldingModel();
		fm.runBatchFoldingOperation(() -> {
			for (FoldRegion r : fm.getGroupedRegions(RegionFold.TAILWIND_GROUP))
				r.setExpanded(shouldExpand);
		});
	}
	
	public static void unfade(VirtualFile vFile) {
		setFade(vFile, CoreState.FadingMode.OFF);
	}
}
