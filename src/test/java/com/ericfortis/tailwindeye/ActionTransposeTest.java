package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ActionTransposeTest extends BasePlatformTestCase {
	public void testToMultiline() {
		myFixture.configureByText("test.jsx", "<div className=\"flex<caret> flex-col\"></div>");
		myFixture.testAction(new ActionTranspose());
		myFixture.checkResult("<div className={`\nflex\nflex-col\n`}></div>");
	}

	public void testToInline() {
		myFixture.configureByText("test.tsx", "<div className={`flex<caret> flex-col`}></div>");
		myFixture.testAction(new ActionTranspose());
		myFixture.checkResult("<div className=\"flex flex-col\"></div>");
	}
}