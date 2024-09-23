package org.eclipse.dataspacetck.core.system;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

class ConfigFunctionsTest {
    private static final String PROP_1 = "PROP1";
    private static final String PROP_1_VALUE = "PROP_1_VALUE";

    @BeforeAll
    static void beforeAll() {
        System.setProperty(PROP_1, PROP_1_VALUE);
    }

    @AfterAll
    static void afterAll() {
        System.clearProperty(PROP_1);
    }

    @Test
    void verifySettings() {
        assertThat(ConfigFunctions.propertyOrEnv("notthere", "default")).isEqualTo("default");
        assertThat(ConfigFunctions.propertyOrEnv(PROP_1, "notthere")).isEqualTo(PROP_1_VALUE);

        if (!System.getenv().isEmpty()) {
            var entry = System.getenv().entrySet().iterator().next();
            assertThat(ConfigFunctions.propertyOrEnv(entry.getKey().toLowerCase(), null)).isEqualTo(entry.getValue());
        }
    }
}