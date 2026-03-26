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
		FOLD_CLASS_NAME;

		public FadingMode next() {
			return this == NON_STYLING ? FOLD_CLASS_NAME : NON_STYLING;
		}
	}

	public static final class InnerState {
		public FadingMode fadingMode = FadingMode.FOLD_CLASS_NAME;
	}

	private InnerState state = new InnerState();

	public static CoreState getInstance(@NotNull Project project) {
		return project.getService(CoreState.class);
	}

	@Override
	public @Nullable InnerState getState() {
		return state;
	}

	@Override
	public void loadState(@NotNull InnerState state) {
		this.state = state;
	}

	public FadingMode getFadingMode() {
		return state.fadingMode;
	}

	public FadingMode toggleFadingMode() {
		state.fadingMode = state.fadingMode.next();
		return state.fadingMode;
	}

	public void setFadingMode(FadingMode mode) {
		state.fadingMode = mode;
	}
}
