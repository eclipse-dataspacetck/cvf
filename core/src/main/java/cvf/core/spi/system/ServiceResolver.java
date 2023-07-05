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
 * Resolves services.
 */
@FunctionalInterface
public interface ServiceResolver {

    /**
     * Resolves an instance of the type or null if not found.
     */
    @Nullable
    Object resolve(Class<?> type, ServiceConfiguration configuration);
}
