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

package org.eclipse.dataspacetck.core.api.system;

/**
 * Constants for system configuration.
 */
public interface SystemsConstants {
    String TCK_PREFIX = "dataspacetck";
    String TCK_CALLBACK_ADDRESS = TCK_PREFIX + ".callback.address";
    String TCK_DEFAULT_CALLBACK_ADDRESS = "http://localhost:8083";
    String TCK_LAUNCHER = TCK_PREFIX + ".launcher";
}
