package com.ericfortis.tailwindeye;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.PROJECT)
@com.intellij.openapi.components.State(name = "State", storages = @Storage("tailwindEye.xml"))
public final class CoreState implements PersistentStateComponent<CoreState.InnerState> {

    public enum FadingMode {
        NON_STYLING,
        FOLD_CLASS_NAME
    }

    public static class InnerState {
        public FadingMode fadingMode = FadingMode.FOLD_CLASS_NAME;
    }

    private InnerState myState = new InnerState();

    public static CoreState getInstance(@NotNull Project project) {
        return project.getService(CoreState.class);
    }

    @Override
    public @Nullable InnerState getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull InnerState state) {
        myState = state;
    }

    public FadingMode getFadingMode() {
        return myState.fadingMode;
    }

    public void setFadingMode(FadingMode mode) {
        myState.fadingMode = mode;
    }
}
