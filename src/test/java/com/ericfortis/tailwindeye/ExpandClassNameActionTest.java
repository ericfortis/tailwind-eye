package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ExpandClassNameActionTest extends BasePlatformTestCase {
	public final String inline = "\"flex flex-col\"";
	public final String multiline = "{`\nflex\nflex-col\n`}";

	public void testToMultiline() {
		assertEquals(ExpandClassNameAction.toggle(inline), multiline);
	}

	public void testToInline() {
		assertEquals(ExpandClassNameAction.toggle(multiline), inline);
	}
}