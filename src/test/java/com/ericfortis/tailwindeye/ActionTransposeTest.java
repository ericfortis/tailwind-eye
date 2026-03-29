package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ActionTransposeTest extends BasePlatformTestCase {
	public final String inline = "\"flex flex-col\"";
	public final String multiline = "{`\nflex\nflex-col\n`}";

	public void testToMultiline() {
		assertEquals(ActionTranspose.toggle(inline), multiline);
	}

	public void testToInline() {
		assertEquals(ActionTranspose.toggle(multiline), inline);
	}
}