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

/**
 * Configuration manipulation functions.
 */
public class ConfigFunctions {

    /**
     * Returns a configuration value by checking system properties and then env variables. Keys are converted to uppercase and
     * "." is replaced by "_" when checking for env variables.
     */
    public static String propertyOrEnv(String key, String defaultValue) {
        var value = System.getProperty(key);
        if (exists(value)) {
            return value;
        }
        var upperKey = key.toUpperCase().replace('.', '_');
        value = System.getenv(upperKey);
        if (exists(value)) {
            return value;
        }
        return defaultValue;
    }

    private static boolean exists(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private ConfigFunctions() {
    }
}
