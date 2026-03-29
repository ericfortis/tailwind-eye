package com.ericfortis.tailwindeye;

import com.intellij.lang.javascript.JSBaseEditorTestCase;

public class ExpandClassNameActionTest extends JSBaseEditorTestCase {
	@Override
	protected String getTestDataPath() {
		return "";
	}

	public void testJsxFileTypeResolution() {
		myFixture.configureByText("test.jsx", "<div className=\"x\"></div>");
		assertFalse("PLAIN_TEXT".equals(myFixture.getFile().getFileType().getName()));
		assertFalse("TEXT".equals(myFixture.getFile().getLanguage().getID()));
	}

	public void testToMultiline() {
		myFixture.configureByText("test.jsx", "<div className=\"my-2 <caret>flex flex-col\"></div>");
		myFixture.testAction(new ExpandClassNameAction());
		myFixture.checkResult("<div className={`\nmy-2\nflex\nflex-col\n`}></div>");
	}

	public void testToInline() {
		myFixture.configureByText("test.jsx", "<div className={`my-2 <caret>flex flex-col`}></div>");
		myFixture.testAction(new ExpandClassNameAction());
		myFixture.checkResult("<div className=\"my-2 flex flex-col\"></div>");
	}
}
