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

package cvf.core.spi.system;

import org.jetbrains.annotations.Nullable;

/**
 * Initializes and interfaces with the system being verified.
 */
public interface SystemLauncher {

    /**
     * Performs required initialization and signals to the system that a test run will start.
     */
    void start(SystemConfiguration configuration);

    /**
     * Signals that the test run has completed and resources may be freed.
     */
    default void close() {
    }

    /**
     * Returns true if the launcher can provide a service of the given type.
     */
    default <T> boolean providesService(Class<T> type) {
        return false;
    }

    /**
     * Returns a service of the given type or null. Some services may not be available until {@link #start(SystemConfiguration)} ()} is invoked.
     *
     * @param type the type of service to inject.
     * @param configuration the configuration used to resolve the service
     * @param resolver a resolver to resolve dependencies that may be required for the requested service
     */
    @Nullable
    default <T> T getService(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        return null;
    }

}
