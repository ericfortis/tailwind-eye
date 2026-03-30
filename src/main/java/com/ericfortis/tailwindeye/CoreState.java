package com.ericfortis.tailwindeye;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
@com.intellij.openapi.components.State(name = "State", storages = @Storage("tailwindEye.xml"))
public final class CoreState implements PersistentStateComponent<CoreState> {

	public static final Key<EyeMode> EYE_MODE_KEY = Key.create("TAILWIND_EYE_FADING_MODE");

	public enum EyeMode {
		OFF,
		FADE,
		FOLD;

		public EyeMode next() {
			if (this == OFF) 
				return FOLD;
			return this == FADE 
				 ? FOLD 
				 : FADE;
		}
	}

	private final EyeMode mode = EyeMode.FOLD;

	public EyeMode getMode() {
		return mode;
	}

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
}
