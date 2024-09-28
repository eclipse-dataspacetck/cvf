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
 * Proxy to the provider connector being verified for contract negotiation.
 */
public interface ProviderNegotiationClient {

    /**
     * Creates a contract request. Used for initial requests and client counter-offers.
     */
    Map<String, Object> contractRequest(Map<String, Object> message, boolean expectError);

    /**
     * Accepts the most recent offer.
     */
    void accept(Map<String, Object> offer);

    /**
     * Verifies the contract agreement with the provider.
     */
    void verify(Map<String, Object> verification, boolean expectError);

    /**
     * Terminates a negotiation.
     */
    void terminate(Map<String, Object> termination, boolean expectError);

    /**
     * Returns a negotiation.
     */
    Map<String, Object> getNegotiation(String processId);

}
