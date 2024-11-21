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

package org.eclipse.dataspacetck.rendering.spi;

import org.eclipse.dataspacetck.document.model.Category;
import org.eclipse.dataspacetck.document.model.TestCase;
import org.eclipse.dataspacetck.document.model.TestSuite;

/**
 * Renders individual items of a given test plan (represented by a {@link org.eclipse.dataspacetck.document.model.TestGraph})
 */
public interface TestPlanRenderer {

    /**
     * The title section of the testplan document
     */
    void title(String title);

    /**
     * Renders a subtitle of the testplan
     */
    void subTitle(String subTitle);

    /**
     * Renders a category of tests. Typically, this encompasses several test suites.
     */
    void category(Category category);

    /**
     * renders a test suite
     */
    void testSuite(TestSuite testSuite);

    /**
     * renders an individual test case
     */
    void testCase(TestCase testCase);

    /**
     * converts the internal structure into a string representation
     */
    String render();
}
