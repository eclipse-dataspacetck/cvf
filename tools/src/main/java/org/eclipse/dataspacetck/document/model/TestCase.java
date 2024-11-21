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

/**
 * Represents one particular test case, i.e. a JUnit test method.
 *
 * @param methodName  The name of the method. This should be the test number
 * @param displayName An arbitrary string that describes what is being tested.
 * @param number      A unique number identifying the test within the TCK
 * @param isMandatory Whether the test is mandatory
 * @param diagramCode Formatted PlantUML or Mermaid code to render a diagram
 */
public record TestCase(String methodName, String displayName, String number, boolean isMandatory, String diagramCode) {

    public static final String DSP_SPEC_URL = "https://foo.bar/spec/";

    public String specUrl() {
        return DSP_SPEC_URL + number;
    }
}
