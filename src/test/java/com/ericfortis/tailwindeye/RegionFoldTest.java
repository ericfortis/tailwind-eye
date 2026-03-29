package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class RegionFoldTest extends BasePlatformTestCase {
	public void testFoldingRegions() {
		myFixture.configureByText("test.html",
			 "<tag className=\"bg-red-500 p-4\">Hello</tag>");

		var builder = new RegionFold();
		var descriptors = builder.buildFoldRegions(
			 myFixture.getFile(),
			 myFixture.getEditor().getDocument(),
			 false
		);

		var found = false;
		for (var descriptor : descriptors) {
			assertEquals(RegionFold.PLACEHOLDER, builder.getPlaceholderText(descriptor.getElement()));
			found = true;
			break;
		}
		assertTrue("Folding descriptor for className should be found", found);
	}


	public void testNoFoldingForOtherAttributes() {
		myFixture.configureByText("test.html",
			 "<tag id=\"main\" title=\"Home\">Hello</tag>");

		var builder = new RegionFold();
		var descriptors = builder.buildFoldRegions(
			 myFixture.getFile(),
			 myFixture.getEditor().getDocument(),
			 false
		);
		assertEquals("Should have no folding descriptors", 0, descriptors.length);
	}
}
