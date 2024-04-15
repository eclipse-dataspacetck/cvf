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

package org.eclipse.dataspacetck.dsp.system.api.client;

import java.util.Map;

/**
 * Proxy to an identity provider trusted by the client and provider.
 */
public interface IdentityProviderClient {

    /**
     * Returns security headers for the test key.
     */
    Map<String, String> getSecurityHeaders(String key);

}
