package org.eclipse.dataspacetck.annotation.processors.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class Category {
    private final String name;
    private final Map<String, TestSuite> suites = new HashMap<>();

    public Category(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public Collection<TestSuite> suites() {
        return suites.values();
    }

    public TestSuite testSuite(String suiteName) {
        return suites.computeIfAbsent(suiteName, s -> new TestSuite(suiteName));
    }
}
