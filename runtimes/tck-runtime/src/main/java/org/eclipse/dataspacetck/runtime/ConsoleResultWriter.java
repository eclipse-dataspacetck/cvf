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

package org.eclipse.dataspacetck.runtime;

import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static java.lang.Boolean.parseBoolean;

/**
 * Outputs to the console.
 */
public class ConsoleResultWriter {
    private boolean ansi;

    public ConsoleResultWriter() {
        ansi = parseBoolean(System.getProperty("cvf.ansi", "true"));
    }

    public void output(TestExecutionSummary result) {
        System.out.println("\nSuccessful tests: " + result.getTestsSucceededCount());
        System.out.println("Failed tests: " + result.getTestsFailedCount());
        if (!result.getFailures().isEmpty()) {
            System.out.printf("%n%sFailures:%n", ansiError());
            result.getFailures().forEach(f -> System.out.println("   -" + f.getException().getMessage()));
            System.out.println(ansiReset());
        } else {
            System.out.printf("%sTest suite completed successfully%n", ansiSuccess());
            System.out.println(ansiReset());
        }
    }

    private String ansiError() {
        return ansi ? "\u001B[31m" : "";
    }

    private String ansiSuccess() {
        return ansi ? "\u001B[32m" : "";
    }

    private String ansiReset() {
        return ansi ? "\u001B[0m" : "";
    }
}
