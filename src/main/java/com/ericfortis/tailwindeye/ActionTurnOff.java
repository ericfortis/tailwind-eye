package com.ericfortis.tailwindeye;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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

		ActionFoldOrFade.unfold(editor);
		RegionFade.updateFade(editor, psiFile, false);

		ActionFoldOrFade.unfade(vFile);
	}
}
