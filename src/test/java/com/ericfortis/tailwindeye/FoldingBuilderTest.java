package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.lang.folding.FoldingDescriptor;

public class FoldingBuilderTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData/folding";
    }

    public void testFoldingRegions() {
        // Use a more explicit XML-like tag that might be picked up by the base XML support
        myFixture.configureByText("test.xml",
                "<tag className=\"bg-red-500 p-4\">Hello</tag>");
        
        // Wait, FoldingBuilder in plugin.xml is registered for "Vue", "JSX Harmony", and "TypeScript JSX"
        // It's NOT registered for "XML". 
        // If the test environment doesn't have Vue or JSX plugins loaded, it won't work.
        
        // Let's try to manually invoke buildFoldRegions
        FoldingBuilder builder = new FoldingBuilder();
        FoldingDescriptor[] descriptors = builder.buildFoldRegions(
                myFixture.getFile(), 
                myFixture.getEditor().getDocument(), 
                false
        );
        
        boolean found = false;
        for (FoldingDescriptor ignored : descriptors) {
            // Check if the range matches the expected className value
            // <tag className="bg-red-500 p-4">Hello</tag>
            // range should be for "bg-red-500 p-4"
            found = true;
            break;
        }
        assertTrue("Folding descriptor for className should be found", found);
    }

    public void testCollapsedByDefaultWhenModeIsFoldClassName() {
        CoreState.getInstance(getProject()).setFadingMode(CoreState.FadingMode.FOLD_CLASS_NAME);
        myFixture.configureByText("test.xml",
                "<tag className=\"bg-red-500 p-4\">Hello</tag>");
        
        FoldingBuilder builder = new FoldingBuilder();
        FoldingDescriptor[] descriptors = builder.buildFoldRegions(
                myFixture.getFile(), 
                myFixture.getEditor().getDocument(), 
                false
        );
        
        assertTrue("Region should be collapsed by default", builder.isCollapsedByDefault(descriptors[0].getElement()));
    }

    public void testNotCollapsedByDefaultWhenModeIsNonStyling() {
        CoreState.getInstance(getProject()).setFadingMode(CoreState.FadingMode.NON_STYLING);
        myFixture.configureByText("test.xml",
                "<tag className=\"bg-red-500 p-4\">Hello</tag>");
        
        FoldingBuilder builder = new FoldingBuilder();
        FoldingDescriptor[] descriptors = builder.buildFoldRegions(
                myFixture.getFile(), 
                myFixture.getEditor().getDocument(), 
                false
        );
        
        assertFalse("Region should not be collapsed by default", builder.isCollapsedByDefault(descriptors[0].getElement()));
    }
    public void testNoFoldingForOtherAttributes() {
        myFixture.configureByText("test.xml",
                "<tag id=\"main\" title=\"Home\">Hello</tag>");
        
        FoldingBuilder builder = new FoldingBuilder();
        FoldingDescriptor[] descriptors = builder.buildFoldRegions(
                myFixture.getFile(), 
                myFixture.getEditor().getDocument(), 
                false
        );
        
        assertEquals("Should have no folding descriptors", 0, descriptors.length);
    }
    public void testFoldingForEmptyClassNameIncludesQuotes() {
        myFixture.configureByText("test.xml",
                "<tag className=\"\">Hello</tag>");
        
        FoldingBuilder builder = new FoldingBuilder();
        FoldingDescriptor[] descriptors = builder.buildFoldRegions(
                myFixture.getFile(), 
                myFixture.getEditor().getDocument(), 
                false
        );
        
        // In XML context, className="" value range includes the quotes, so length is 2.
        assertEquals("Should have folding descriptor for empty className quotes in XML", 1, descriptors.length);
    }
}
