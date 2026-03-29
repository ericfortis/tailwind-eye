package com.ericfortis.tailwindeye;

import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class RegionFoldTest extends BasePlatformTestCase {
	public void testFoldingRegions() {
		myFixture.configureByText("test.html",
			 "<tag className=\"bg-red-500 p-4\">Hello</tag>");

		RegionFold builder = new RegionFold();
		FoldingDescriptor[] descriptors = builder.buildFoldRegions(
			 myFixture.getFile(),
			 myFixture.getEditor().getDocument(),
			 false
		);

		boolean found = false;
		for (FoldingDescriptor descriptor : descriptors) {
			assertEquals("…", builder.getPlaceholderText(descriptor.getElement()));
			found = true;
			break;
		}
		assertTrue("Folding descriptor for className should be found", found);
	}


	public void testNoFoldingForOtherAttributes() {
		myFixture.configureByText("test.html",
			 "<tag id=\"main\" title=\"Home\">Hello</tag>");

		RegionFold builder = new RegionFold();
		FoldingDescriptor[] descriptors = builder.buildFoldRegions(
			 myFixture.getFile(),
			 myFixture.getEditor().getDocument(),
			 false
		);
		assertEquals("Should have no folding descriptors", 0, descriptors.length);
	}


	public void testFoldingForEmptyClassNameIncludesQuotes() {
		myFixture.configureByText("test.html",
			 "<tag className=\"\">Hello</tag>");

		RegionFold builder = new RegionFold();
		FoldingDescriptor[] descriptors = builder.buildFoldRegions(
			 myFixture.getFile(),
			 myFixture.getEditor().getDocument(),
			 false
		);
		assertEquals("Should have folding descriptor for empty className quotes in XML", 1, descriptors.length);
		assertEquals("…", builder.getPlaceholderText(descriptors[0].getElement()));
	}
}
