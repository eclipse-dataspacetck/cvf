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
 * Manages contract negotiations on a provider.
 */
public interface ProviderNegotiationManager extends NegotiationManager {

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#OFFERED} state.
     */
    void offered(String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#AGREED} state.
     */
    void agreed(String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#FINALIZED} state.
     */
    void finalized(String providerId);

    /**
     * Transitions the negotiation to {@link ContractNegotiation.State#FINALIZED} state.
     */
    void terminated(Map<String, Object> termination);

    /**
     * Called when a contract request is received.
     */
    Map<String, Object> handleContractRequest(Map<String, Object> contractRequest, String counterPartyId);

    /**
     * Processes an accepted event received from the consumer.
     */
    void handleAccepted(Map<String, Object> event);

    /**
     * Processes a verification received from the consumer.
     */
    void handleVerified(Map<String, Object> verification);


}
