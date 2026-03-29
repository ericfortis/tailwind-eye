package com.ericfortis.tailwindeye;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ExpandClassNameActionTest extends BasePlatformTestCase {
	@Override
	protected String getTestDataPath() {
		return "src/test/testData";
	}
	
	@Override
	protected boolean isWriteActionRequired() {
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PluginManagerCore.getLoadedPlugins().forEach(p ->
			 System.out.println("Loaded: " + p.getPluginId())
		);
	}

	public void testJsxFileTypeResolution() {
		myFixture.configureByText("test.jsx", "<div className=\"x\"></div>");
		String fileTypeName = myFixture.getFile().getFileType().getName();
		String languageId = myFixture.getFile().getLanguage().getID();
		System.out.println("FileType: " + fileTypeName + ", Language: " + languageId);
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