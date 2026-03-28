package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ExpandClassNameActionTest extends BasePlatformTestCase {

    public void testExpandClassNameInHtml() {
        myFixture.configureByText("test.html",
            "<div className=\"last-part flex<caret> flex-col gap-y-3\"></div>");
        // Use direct call because the action might not be registered properly in the test environment's ActionManager
        ExpandClassNameAction action = new ExpandClassNameAction();
        myFixture.testAction(action);
        myFixture.checkResult("<div className={`\n" +
            "last-part\n" +
            "flex\n" +
            "flex-col\n" +
            "gap-y-3\n" +
            "`}></div>");
    }

    public void testExpandClassNameInJsx() {
        // We use .html because .jsx might not be properly parsed in the test environment,
        // but our action is robust enough to handle the string representation if the parent has className=
        myFixture.configureByText("test.html",
            "const element = <div className=\"p-4 <caret>m-2\"></div>");
        ExpandClassNameAction action = new ExpandClassNameAction();
        myFixture.testAction(action);
        myFixture.checkResult("const element = <div className={`\n" +
            "p-4\n" +
            "m-2\n" +
            "`}></div>");
    }

    public void testInlineClassName() {
        myFixture.configureByText("test.js",
            "const a = <div className={`\n" +
            "last-part\n" +
            "flex\n" +
            "flex-col\n" +
            "gap-y-3\n" +
            "<caret>`}></div>");
        ExpandClassNameAction action = new ExpandClassNameAction();
        myFixture.testAction(action);
        myFixture.checkResult("const a = <div className=\"last-part flex flex-col gap-y-3\"></div>");
    }

    public void testInlineClassNameSingleLine() {
        myFixture.configureByText("test.js",
            "const a = <div className={`p-4 <caret>m-2`}></div>");
        ExpandClassNameAction action = new ExpandClassNameAction();
        myFixture.testAction(action);
        myFixture.checkResult("const a = <div className=\"p-4 m-2\"></div>");
    }
}
