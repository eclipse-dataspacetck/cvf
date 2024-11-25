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

import org.eclipse.dataspacetck.rendering.spi.TestPlanRenderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TestGraph {
    private final Map<String, Category> categories = new HashMap<>();

    public Category category(String category) {
        return categories.computeIfAbsent(category, s -> new Category(category));
    }

    public Collection<Category> categories() {
        return categories.values();
    }

    public void render(TestPlanRenderer renderer) {
        categories()
                .forEach(cat -> {
                    renderer.category(cat);
                    cat.suites().forEach(suite -> {
                        renderer.testSuite(suite);
                        suite.testMethods().forEach(renderer::testCase);
                    });
                });
    }
}
