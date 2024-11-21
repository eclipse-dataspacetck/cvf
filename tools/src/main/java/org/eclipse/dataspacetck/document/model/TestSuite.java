package org.eclipse.dataspacetck.document.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class TestSuite {
    private final String name;
    private final Map<String, TestMethod> testMethods = new HashMap<>();

    public TestSuite(String displayName) {
        this.name = displayName;
    }

    public String name() {
        return name;
    }

    public Collection<TestMethod> testMethods() {
        return testMethods.values();
    }

    public void insert(TestMethod testMethod) {
        testMethods.put(testMethod.number(), testMethod);
    }
}
