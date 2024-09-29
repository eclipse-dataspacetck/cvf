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

package org.eclipse.dataspacetck.dsp.system.client;

import java.util.Map;

/**
 * Proxy to the provider connector being verified for contract negotiation.
 */
public interface ProviderNegotiationClient {

    /**
     * Sends the contract request to the provider. Used for initial requests and client counter-offers.
     */
    Map<String, Object> contractRequest(Map<String, Object> message, boolean expectError);

    /**
     * Sends the accepted event to the provider connector.
     */
    void accept(Map<String, Object> event);

    /**
     * Sends the verified event to the provider connector.
     */
    void verify(Map<String, Object> event, boolean expectError);

    /**
     * Terminates the negotiation with the provider.
     */
    void terminate(Map<String, Object> termination, boolean expectError);

    /**
     * Retrieves the negotiation from the provider.
     */
    Map<String, Object> getNegotiation(String processId);

}
