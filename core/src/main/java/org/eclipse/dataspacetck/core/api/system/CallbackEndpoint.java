/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

import java.util.function.Function;

/**
 * An endpoint to receive asynchronous responses.
 */
public interface CallbackEndpoint {

    /**
     * The callback base address, for example <code>https://test.com</code>.
     */
    String getAddress();

    /**
     * Registers a response handler for the given callback path.
     */
    void registerHandler(String path, Function<Object, Object> consumer);

    /**
     * Deregisters a response handler.
     */
    void deregisterHandler(String path);

}
