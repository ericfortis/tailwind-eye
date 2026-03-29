package com.ericfortis.tailwindeye;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

import static com.intellij.lang.Language.ANY;

public class SettingsPage implements ColorSettingsPage {
	private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
		 new AttributesDescriptor("Faint text", TextAttributesKey.createTextAttributesKey("TAILWIND_EYE_FAINT")),
	};

	@Override
	public @Nullable Icon getIcon() {
		return null;
	}

	@Override
	public @NotNull SyntaxHighlighter getHighlighter() {
		return SyntaxHighlighterFactory.getSyntaxHighlighter(ANY, null, null);
	}

	@Override
	public @NotNull String getDemoText() {
		return """
			 <faint>function Example() {
			   return (
			     <</faint>div <faint>className="</faint>bg-blue-500 p-4 text-white font-bold<faint>">
			       Hello World
			     </div>
			   );
			 }</faint>""";
	}

	@Override
	public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
		return Map.of(
			 "faint", TextAttributesKey.createTextAttributesKey("TAILWIND_EYE_FAINT")
		);
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
