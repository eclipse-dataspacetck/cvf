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

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static java.lang.String.format;

/**
 * Outputs to the console.
 */
public class ConsoleResultWriter {
    private Monitor monitor;

    public ConsoleResultWriter(Monitor monitor) {
        this.monitor = monitor;
    }

    public void output(TestExecutionSummary result) {
        monitor.message("Passed tests: " + result.getTestsSucceededCount());
        monitor.message("Failed tests: " + result.getTestsFailedCount());
        if (!result.getFailures().isEmpty()) {
            monitor.enableError().message("Failures:");
            result.getFailures()
                    .stream()
                    .filter(f -> f.getTestIdentifier().getSource().isPresent() &&
                            f.getTestIdentifier().getSource().get() instanceof MethodSource)
                    .forEach(f -> {
                        var method = (MethodSource) f.getTestIdentifier().getSource().get();
                        monitor.message(format("\n   %c %s.%s\n", '■', method.getClassName(), method.getMethodName()));
                        monitor.message("     [" + f.getTestIdentifier().getDisplayName() + "]");
                        monitor.message("     " + f.getException().getMessage() + "\n");
                    });
            monitor.resetMode();
        } else {
            monitor.enableSuccess().message("🎉😃🚀All tests passed").resetMode();
        }
    }

}
