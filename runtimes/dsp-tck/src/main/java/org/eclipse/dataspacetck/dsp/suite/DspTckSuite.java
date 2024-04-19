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

import org.eclipse.dataspacetck.runtime.TckRuntime;

/**
 *
 */
public class DspTckSuite {
    public static void main(String... args) {
        var result = TckRuntime.Builder.newInstance()
                .launcherClass("org.eclipse.dataspacetck.dsp.system.DspSystemLauncher")
                .property("dataspacetck.dsp.local.connector", "true")
                .addPackage("org.eclipse.dataspacetck")
                .build().execute();

        System.out.println();
    }
}
