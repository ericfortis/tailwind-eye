package com.ericfortis.tailwindeye;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class ExpandClassNameActionTest extends BasePlatformTestCase {

    public void testExpandClassNameInHtml() {
        myFixture.configureByText("test.html",
            "<div className=\"last-part flex<caret> flex-col gap-y-3\"></div>");
			myFixture.testAction(new ExpandClassNameAction());
        myFixture.checkResult("""
					 <div className={`
					 last-part
					 flex
					 flex-col
					 gap-y-3
					 `}></div>""");
    }

    public void testExpandClassNameInJsx() {
        myFixture.configureByText("test.xml",
            "<div className=\"p-4 <caret>m-2\"></div>");
			myFixture.testAction(new ExpandClassNameAction());
        myFixture.checkResult("""
					 <div className={`
					 p-4
					 m-2
					 `}></div>""");
    }
}
