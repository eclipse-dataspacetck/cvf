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
 * Represents a test category. This could be determined by a JUnit {@link org.junit.jupiter.api.Tag} annotation
 */
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
