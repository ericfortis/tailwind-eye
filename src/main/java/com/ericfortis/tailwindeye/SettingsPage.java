package com.ericfortis.tailwindeye;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class SettingsPage implements com.intellij.openapi.options.colors.ColorSettingsPage {
	private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
		 new AttributesDescriptor("Faded text", TextAttributesKey.createTextAttributesKey("TAILWIND_EYE_TEXT"))
	};

	@Override
	public @Nullable Icon getIcon() {
		return null;
	}

	@Override
	public @NotNull com.intellij.openapi.fileTypes.SyntaxHighlighter getHighlighter() {
		return com.intellij.openapi.fileTypes.SyntaxHighlighterFactory.getSyntaxHighlighter(com.intellij.lang.Language.ANY, null, null);
	}

	@Override
	public @NotNull String getDemoText() {
		return """
			 <faded>function Example() {
			      return (</faded>
			     <div <faded>className="</faded>bg-blue-500 p-4 text-white font-bold<faded>">
			       Hello World
			     </div>
			   );
			 }</faded>""";
	}

	@Override
	public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
		return Map.of("faded", TextAttributesKey.createTextAttributesKey("TAILWIND_EYE_TEXT"));
	}

	@Override
	public @NotNull AttributesDescriptor @NotNull [] getAttributeDescriptors() {
		return DESCRIPTORS;
	}

	@Override
	public @NotNull ColorDescriptor @NotNull [] getColorDescriptors() {
		return ColorDescriptor.EMPTY_ARRAY;
	}

	@Override
	public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
		return "Tailwind Eye";
	}
}
