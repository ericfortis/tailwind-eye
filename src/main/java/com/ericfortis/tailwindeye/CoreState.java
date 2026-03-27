package com.ericfortis.tailwindeye;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
@com.intellij.openapi.components.State(name = "State", storages = @Storage("tailwindEye.xml"))
public final class CoreState implements PersistentStateComponent<CoreState> {

	public enum FadingMode {
		NON_STYLING,
		FOLD_CLASS_NAME;

		public FadingMode next() {
			return this == NON_STYLING ? FOLD_CLASS_NAME : NON_STYLING;
		}
	}

	public FadingMode fadingMode = FadingMode.FOLD_CLASS_NAME;

	public static CoreState getInstance(@NotNull Project project) {
		return project.getService(CoreState.class);
	}

	@Override
	public CoreState getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull CoreState state) {
		XmlSerializerUtil.copyBean(state, this);
	}

	public boolean isFading() {
		return fadingMode == FadingMode.NON_STYLING;
	}

	public FadingMode toggleFadingMode() {
		fadingMode = fadingMode.next();
		return fadingMode;
	}
}
