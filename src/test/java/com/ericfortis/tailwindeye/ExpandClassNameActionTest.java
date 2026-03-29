package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

// TODO it should work with tsx or jsx
public class ExpandClassNameActionTest extends BasePlatformTestCase {
	public void testToMultiline() {
		myFixture.configureByText("test.tsx", "<div className=\"my-2 <caret>flex flex-col\"></div>");
		myFixture.testAction(new ExpandClassNameAction());
		myFixture.checkResult("<div className={`\nmy-2\nflex\nflex-col\n`}></div>");
	}

	public void testToInline() {
		myFixture.configureByText("test.tsx", "<div className={`my-2 <caret>flex flex-col`}></div>");
		myFixture.testAction(new ExpandClassNameAction());
		myFixture.checkResult("<div className=\"my-2 flex flex-col\"></div>");
	}
}
