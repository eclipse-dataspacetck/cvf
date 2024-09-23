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

package org.eclipse.dataspacetck.runtime;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import static java.lang.String.format;

/**
 * Provides progress output during test execution.
 */
class TckExecutionListener implements TestExecutionListener {
    private Monitor monitor;

    TckExecutionListener(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void executionStarted(TestIdentifier identifier) {
        if (identifier.getSource().isPresent() && identifier.getSource().get() instanceof MethodSource) {
            monitor.newLine().message("Started: " + identifier.getDisplayName());
        }
    }

    @Override
    public void executionFinished(TestIdentifier identifier, TestExecutionResult result) {
        if (identifier.getSource().isPresent() && identifier.getSource().get() instanceof MethodSource) {
            var displayName = identifier.getDisplayName();
            if (displayName.contains(":")) {
                displayName = displayName.substring(0, displayName.lastIndexOf(":"));
            }
            monitor.message(format("%s: %s%n", result.getStatus(), displayName));
        }
    }

}
