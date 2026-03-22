package com.github.ericfortis.tailwindeye;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ToggleAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        State state = State.getInstance(project);
        State.FadingMode currentMode = state.getFadingMode();
        State.FadingMode nextMode;

        switch (currentMode) {
            case NON_STYLING -> nextMode = State.FadingMode.FOLD_CLASS_NAME;
            case FOLD_CLASS_NAME -> nextMode = State.FadingMode.NON_STYLING;
            default -> nextMode = State.FadingMode.NON_STYLING;
        }

        state.setFadingMode(nextMode);

        // Refresh highlighting and folding in all open files
        DaemonCodeAnalyzer.getInstance(project).restart();
        
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            FoldingModelEx foldingModel = (FoldingModelEx) editor.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> {
                for (com.intellij.openapi.editor.FoldRegion region : foldingModel.getAllFoldRegions()) {
                    if (FoldingBuilder.TAILWIND_GROUP.equals(region.getGroup())) {
                        region.setExpanded(nextMode != State.FadingMode.FOLD_CLASS_NAME);
                    }
                }
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

        State.FadingMode mode = State.getInstance(project).getFadingMode();
        String text = switch (mode) {
            case NON_STYLING -> "Switch to Folding (className)";
            case FOLD_CLASS_NAME -> "Switch to Fading (Non-styling)";
        };
        e.getPresentation().setText(text);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
