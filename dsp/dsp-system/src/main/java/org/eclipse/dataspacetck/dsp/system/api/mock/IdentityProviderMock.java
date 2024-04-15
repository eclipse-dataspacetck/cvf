/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package org.eclipse.dataspacetck.dsp.system.api.mock;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Mock identity provider.
 */
public interface IdentityProviderMock {

    /**
     * Records a supplier that will be used for all requests of the given key.
     */
    void record(String key, Supplier<Map<String, String>> supplier);

    /**
     * Records a supplier that will be used the specified times when the give key is requested.
     */
    void record(String key, Supplier<Map<String, String>> supplier, int times);

}
