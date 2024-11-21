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

class TestGraphTest {

    @Test
    void category_whenNotExists() {
        var g = new TestGraph();
        g.category("test-category");
        assertThat(g.categories()).hasSize(1);
    }

    @Test
    void category_whenExists() {
        var g = new TestGraph();
        g.category("test-category");
        g.category("test-category");
        assertThat(g.categories()).hasSize(1);
    }

    @Test
    void render() {
    }
}