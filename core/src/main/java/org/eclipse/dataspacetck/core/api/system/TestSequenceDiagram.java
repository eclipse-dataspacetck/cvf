/*
 *  Copyright (c) 2023 TNO
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       TNO - initial annotation implementation
 *
 *
 */

package org.eclipse.dataspacetck.core.api.system;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Denotes the sequence diagram of a test.
 * <p>
 * The Mermaid sequence diagram notation should be used to allow automated generation of documentation for tests.
 */
@Inherited
@Retention(SOURCE)
@Target(METHOD)
@Test
public @interface TestSequenceDiagram {
    /**
     * MermaidJS Sequence diagram string, without "sequenceDiagram".
     */
    String value();
}
