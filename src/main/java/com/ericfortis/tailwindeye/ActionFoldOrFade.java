package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.project.Project;
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

		var nextMode = getNextMode(project, vFile);

		setFade(vFile, nextMode);
		RegionFade.updateFade(editor, psiFile, nextMode == CoreState.EyeMode.FADE);

		setFold(editor, nextMode != CoreState.EyeMode.FOLD);
	}

	private static CoreState.EyeMode getNextMode(Project project, VirtualFile vFile) {
		var mode = vFile.getUserData(CoreState.EYE_MODE_KEY);
		if (mode == null)
			mode = CoreState.getInstance(project).getMode();
		return mode.next();
	}

	private static void setFade(VirtualFile vFile, CoreState.EyeMode nextMode) {
		vFile.putUserData(CoreState.EYE_MODE_KEY, nextMode);
	}

	private static void setFold(Editor editor, boolean shouldExpand) {
		var fm = (FoldingModelEx) editor.getFoldingModel();
		fm.runBatchFoldingOperation(() -> {
			for (FoldRegion r : fm.getAllFoldRegions()) {
				var g = r.getGroup();
				if (g != null && RegionFold.TAILWIND_GROUP_NAME.equals(g.toString())) 
					r.setExpanded(shouldExpand);
			}
		});
	}

	public static void unfade(VirtualFile vFile) {
		setFade(vFile, CoreState.EyeMode.OFF);
	}

	public static void unfold(Editor editor) {
		setFold(editor, true);
	}
}
