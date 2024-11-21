/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.document.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a JUnit test suite. This could be a class that contains test methods, or an actual {@link org.junit.platform.suite.api.Suite}
 */
public final class TestSuite {
    private final String name;
    private final Map<String, TestCase> testMethods = new HashMap<>();

    public TestSuite(String displayName) {
        this.name = displayName;
    }

    public String name() {
        return name;
    }

    public Collection<TestCase> testMethods() {
        return testMethods.values();
    }

    public void insert(TestCase testMethod) {
        testMethods.put(testMethod.number(), testMethod);
    }
}
