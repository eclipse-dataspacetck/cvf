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
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;

/**
 * Bootstraps the JUnit platform using the Jupiter engine and executes configured TCK tests.
 */
public class TckRuntime {
    private static final String TEST_POSTFIX = ".*Test";

    private Monitor monitor;

    private List<String> packages = new ArrayList<>();
    private Map<String, String> properties = new HashMap<>();

    public TestExecutionSummary execute() {
        properties.forEach(System::setProperty);

        var summaryListener = new SummaryGeneratingListener();

        var request = LauncherDiscoveryRequestBuilder.request()
                .filters(includeClassNamePatterns(TEST_POSTFIX))
                .selectors(packages.stream().map(DiscoverySelectors::selectPackage).toList())
                .build();

        var launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(new TckExecutionListener(monitor));
        launcher.registerTestExecutionListeners(summaryListener);
        launcher.discover(request);
        launcher.execute(request);

        return summaryListener.getSummary();
    }

    private TckRuntime() {
    }

    public static class Builder {
        private TckRuntime launcher;

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder property(String key, String value) {
            launcher.properties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            launcher.properties.putAll(properties);
            return this;
        }

        public Builder addPackage(String pkg) {
            launcher.packages.add(pkg);
            return this;
        }

        public TckRuntime build() {
            return launcher;
        }

        private Builder() {
            launcher = new TckRuntime();
        }

        public Builder monitor(Monitor monitor) {
            this.launcher.monitor = monitor;
            return this;
        }

    }

}
