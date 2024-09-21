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

package org.eclipse.dataspacetck.core.system;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_PREFIX;

/**
 * A monitor that sends messages to standard out.
 */
public class ConsoleMonitor implements Monitor {
    public static final String ANSI_PROPERTY = TCK_PREFIX + ".ansi";
    public static final String DEBUG_PROPERTY = TCK_PREFIX + ".debug";

    private final boolean debug;
    private final boolean ansi;

    public ConsoleMonitor(boolean debug, boolean ansi) {
        this.debug = debug;
        this.ansi = ansi;
    }

    @Override
    public Monitor newLine() {
        System.out.println();
        return this;
    }

    @Override
    public Monitor debug(String message) {
        if (debug) {
            message(message);
        }
        return this;
    }

    @Override
    public Monitor message(String message) {
        var time = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        System.out.printf("[%s] %s%n", time, message);
        return this;
    }

    @Override
    public Monitor enableSuccess() {
        System.out.println(ansiSuccess());
        return this;
    }

    @Override
    public Monitor enableError() {
        System.out.println(ansiError());
        return this;
    }

    @Override
    public Monitor enableBold() {
        System.out.println(ansiBold());
        return this;
    }

    @Override
    public Monitor resetMode() {
        System.out.println(ansiReset());
        return this;
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

    private String ansiBold() {
        return ansi ? "\u001B[1m" : "";
    }

}
