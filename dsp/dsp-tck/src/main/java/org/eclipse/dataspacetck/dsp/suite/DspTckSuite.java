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

package org.eclipse.dataspacetck.dsp.suite;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.core.system.ConsoleMonitor;
import org.eclipse.dataspacetck.runtime.ConsoleResultWriter;
import org.eclipse.dataspacetck.runtime.TckRuntime;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static java.lang.Boolean.parseBoolean;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_LAUNCHER;
import static org.eclipse.dataspacetck.core.system.ConsoleMonitor.ANSI_PROPERTY;
import static org.eclipse.dataspacetck.core.system.ConsoleMonitor.DEBUG_PROPERTY;

/**
 * Launches the DSP TCK and runs the test suite.
 */
public class DspTckSuite {
    private static final String VERSION = "2024.1";
    private static final String CONFIG = "-config";
    private static final String DEFAULT_LAUNCHER = "org.eclipse.dataspacetck.dsp.system.DspSystemLauncher";
    private static final String TEST_PACKAGE = "org.eclipse.dataspacetck.dsp.verification";

    public static void main(String... args) {
        var properties = processEnv(args);
        if (!properties.containsKey(TCK_LAUNCHER)) {
            properties.put(TCK_LAUNCHER, DEFAULT_LAUNCHER);
        }
        var monitor = createMonitor(properties);
        monitor.enableBold().message("\u001B[1mRunning DSP TCK v" + VERSION + "\u001B[0m").resetMode();
        var result = TckRuntime.Builder.newInstance()
                .properties(properties)
                .addPackage(TEST_PACKAGE)
                .monitor(monitor)
                .build().execute();

        new ConsoleResultWriter(monitor).output(result);

        monitor.resetMode().message("Test run complete");
    }

    @NotNull
    private static Monitor createMonitor(Map<String, String> properties) {
        var ansi = parseBoolean(properties.getOrDefault(ANSI_PROPERTY, "true"));
        var debug = parseBoolean(properties.getOrDefault(DEBUG_PROPERTY, "false"));
        return new ConsoleMonitor(debug, ansi);
    }

    private static Map<String, String> processEnv(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        if (args.length != 2) {
            throw new IllegalArgumentException("Invalid number of arguments: " + args.length);
        }
        if (!CONFIG.equals(args[0])) {
            throw new IllegalArgumentException("Invalid argument: " + args[0]);
        }
        try (var reader = new FileReader(args[1])) {
            var properties = new Properties();
            properties.load(reader);
            //noinspection unchecked,rawtypes
            return (Map) properties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
