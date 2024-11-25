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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    void testSuite_whenNotExists() {
        var cat = new Category("test-cat");
        cat.testSuite("test-suite").insert(new TestCase("m", "d", "n", true, "diag"));

        assertThat(cat.suites()).hasSize(1);
    }

    @Test
    void testSuite_whenExists_shouldAppend() {
        var cat = new Category("test-cat");
        cat.testSuite("test-suite").insert(new TestCase("m", "d", "n", true, "diag"));
        cat.testSuite("test-suite").insert(new TestCase("m2", "d2", "n2", true, "diag"));

        assertThat(cat.suites()).hasSize(1);
    }
}
