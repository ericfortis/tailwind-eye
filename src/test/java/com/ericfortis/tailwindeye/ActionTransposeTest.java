package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import static org.junit.Assert.assertNotEquals;

public class ActionTransposeTest extends BasePlatformTestCase {
	public final String inline = "\"flex flex-col\"";
	public final String multiline = "{`\nflex\nflex-col\n`}";

	public void testToMultiline() {
		assertEquals(ActionTranspose.toggle(inline), multiline);
	}

	public void testToInline() {
		assertEquals(ActionTranspose.toggle(multiline), inline);
	}


	public void testJsxFileTypeResolution() {
		myFixture.configureByText("test.jsx", "<div className=\"x\"></div>");
		String fileTypeName = myFixture.getFile().getFileType().getName();
		String languageId = myFixture.getFile().getLanguage().getID();
		System.out.println("FileType: " + fileTypeName + ", Language: " + languageId);
		assertNotEquals("PLAIN_TEXT", myFixture.getFile().getFileType().getName());
		assertNotEquals("TEXT", myFixture.getFile().getLanguage().getID());
	}

//	public void testToMultiline() {
//		myFixture.configureByText("test.jsx", "<div className=\"my-2 <caret>flex flex-col\"></div>");
//		myFixture.testAction(new ExpandClassNameAction());
//		myFixture.checkResult("<div className={`\nmy-2\nflex\nflex-col\n`}></div>");
//	}
//
//	public void testToInline() {
//		myFixture.configureByText("test.jsx", "<div className={`my-2 <caret>flex flex-col`}></div>");
//		myFixture.testAction(new ExpandClassNameAction());
//		myFixture.checkResult("<div className=\"my-2 flex flex-col\"></div>");
//	}
}