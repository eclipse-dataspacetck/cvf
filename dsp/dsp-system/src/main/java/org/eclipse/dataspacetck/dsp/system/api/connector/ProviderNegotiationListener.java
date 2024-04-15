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

package org.eclipse.dataspacetck.dsp.system.api.connector;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;

import java.util.Map;

/**
 * Implementations can be registered to receive contract negotiation events for a producer.
 */
@SuppressWarnings("unused")
public interface ProviderNegotiationListener extends NegotiationListener {

    /**
     * Invoked after a contract request has been received.
     */
    default void contractRequested(Map<String, Object> contractRequest, ContractNegotiation negotiation) {
    }

    /**
     * Invoked after a consumer agreement has been received.
     */
    default void consumerAgreed(ContractNegotiation negotiation) {
    }

    /**
     * Invoked after a consumer agreement has been received.
     */
    default void consumerVerified(Map<String, Object> verification, ContractNegotiation negotiation) {
    }

}
